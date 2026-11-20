#!/usr/bin/env python3
"""Cria o PostgreSQL e o servico web da Asgasd Digital no Render e publica.

Le a chave de API de RENDER_API_KEY (nunca e escrita em disco nem impressa).
Recursos existentes com o mesmo nome sao reaproveitados, por isso o script
pode ser executado varias vezes sem duplicar nada.

Uso:
    RENDER_API_KEY=... python3 scripts/render_deploy.py

Estado final (ids, URL, palavras-passe geradas) fica em /tmp/render-deploy-state.json.
"""

import json
import os
import secrets
import sys
import time
import urllib.error
import urllib.request

API = "https://api.render.com/v1"
OWNER_ID = os.environ.get("RENDER_OWNER_ID")
NOME_SERVICO = "asgasd-digital"
NOME_BD = "asgasd-db"
# A API espera o repositorio sem esquema (ex.: github.com/dono/repo).
REPO = "github.com/hersonbilajr/asgasd-digital"
RAMO = "main"
REGIAO = os.environ.get("RENDER_REGION", "oregon")
VERSAO_PG = "16"
ADMIN_EMAIL = "admin@asgasd.co.mz"
CLIENTE_EMAIL = "cliente@exemplo.co.mz"
ESTADO = "/tmp/render-deploy-state.json"

CHAVE = os.environ.get("RENDER_API_KEY", "").strip()


def pedido(metodo, caminho, corpo=None):
    dados = json.dumps(corpo).encode() if corpo is not None else None
    req = urllib.request.Request(API + caminho, data=dados, method=metodo)
    req.add_header("Authorization", "Bearer " + CHAVE)
    req.add_header("Accept", "application/json")
    if dados:
        req.add_header("Content-Type", "application/json")
    try:
        with urllib.request.urlopen(req, timeout=90) as resp:
            texto = resp.read().decode()
            return resp.status, (json.loads(texto) if texto else None)
    except urllib.error.HTTPError as erro:
        texto = erro.read().decode()
        print(f"ERRO {metodo} {caminho} -> {erro.code}: {texto[:600]}", flush=True)
        raise SystemExit(1)


def listar(caminho):
    """Le um endpoint paginado e devolve a lista de recursos."""
    itens, cursor = [], None
    while True:
        sep = "&" if "?" in caminho else "?"
        url = f"{caminho}{sep}limit=100" + (f"&cursor={cursor}" if cursor else "")
        _, dados = pedido("GET", url)
        if not isinstance(dados, list):
            return dados
        for entrada in dados:
            itens.append(entrada.get("service") or entrada.get("postgres") or entrada)
        if len(dados) < 100:
            return itens
        cursor = dados[-1].get("cursor")
        if not cursor:
            return itens


def password_forte(prefixo):
    return f"{prefixo}As{secrets.token_hex(4)}g9!{secrets.token_urlsafe(6)}".replace("-", "x").replace("_", "y")


def obter_owner():
    if OWNER_ID:
        return OWNER_ID
    owners = listar("/owners")
    if not owners:
        print("Nenhum workspace visivel para esta chave de API.")
        raise SystemExit(1)
    return owners[0]["id"]


def obter_ou_criar_postgres(owner_id):
    for bd in listar("/postgres"):
        if bd.get("name") == NOME_BD:
            print(f"PostgreSQL existente: {bd['id']} ({bd.get('status')})", flush=True)
            return bd["id"], True

    print(f"A criar PostgreSQL {NOME_BD} (plano free, PostgreSQL {VERSAO_PG})...", flush=True)
    _, bd = pedido("POST", "/postgres", {
        "name": NOME_BD,
        "ownerId": owner_id,
        "plan": "free",
        "region": REGIAO,
        "version": VERSAO_PG,
        "databaseName": "asgasd",
        "databaseUser": "asgasd",
    })
    return bd["id"], False


def esperar_postgres(bd_id, limite=900):
    inicio = time.time()
    while time.time() - inicio < limite:
        _, bd = pedido("GET", f"/postgres/{bd_id}")
        estado = bd.get("status")
        print(f"  PostgreSQL: {estado}", flush=True)
        if estado == "available":
            return bd
        if estado in {"recovery_failed", "unavailable"}:
            raise SystemExit(f"PostgreSQL em estado {estado}")
        time.sleep(15)
    raise SystemExit("Tempo esgotado a esperar pelo PostgreSQL")


def obter_ou_criar_servico(owner_id, env_vars):
    for servico in listar("/services"):
        if servico.get("name") == NOME_SERVICO:
            print(f"Servico existente: {servico['id']}", flush=True)
            return servico["id"], True

    print(f"A criar servico web {NOME_SERVICO} (Docker, plano free)...", flush=True)
    _, servico = pedido("POST", "/services", {
        "type": "web_service",
        "name": NOME_SERVICO,
        "ownerId": owner_id,
        "repo": REPO,
        "branch": RAMO,
        # off: nao exige a GitHub App instalada na conta, e os deploys sao disparados aqui.
        "autoDeployTrigger": "off",
        "envVars": env_vars,
        "serviceDetails": {
            "runtime": "docker",
            "plan": "free",
            "region": REGIAO,
            "healthCheckPath": "/",
            "envSpecificDetails": {
                "dockerfilePath": "./Dockerfile",
                "dockerContext": "./",
            },
        },
    })
    return servico["id"], False


def main():
    if not CHAVE:
        print("RENDER_API_KEY nao esta definida no ambiente.")
        raise SystemExit(2)

    owner_id = obter_owner()
    print(f"Workspace: {owner_id}", flush=True)

    bd_id, bd_existente = obter_ou_criar_postgres(owner_id)
    esperar_postgres(bd_id)
    _, info = pedido("GET", f"/postgres/{bd_id}/connection-info")
    database_url = info["internalConnectionString"]

    credenciais = {}
    if os.path.exists(ESTADO):
        try:
            credenciais = json.load(open(ESTADO)).get("credenciais", {})
        except json.JSONDecodeError:
            credenciais = {}
    admin_password = credenciais.get("admin") or password_forte("Adm")
    cliente_password = credenciais.get("cliente") or password_forte("Cli")

    env_vars = [
        {"key": "SPRING_PROFILES_ACTIVE", "value": "prod"},
        {"key": "DATABASE_URL", "value": database_url},
        {"key": "SEED_DEMO", "value": "true"},
        {"key": "ADMIN_EMAIL", "value": ADMIN_EMAIL},
        {"key": "ADMIN_PASSWORD", "value": admin_password},
        {"key": "CLIENTE_EMAIL", "value": CLIENTE_EMAIL},
        {"key": "CLIENTE_PASSWORD", "value": cliente_password},
    ]

    servico_id, _ = obter_ou_criar_servico(owner_id, env_vars)
    url = None
    for _ in range(20):
        _, servico = pedido("GET", f"/services/{servico_id}")
        url = (servico.get("serviceDetails") or {}).get("url")
        if url:
            break
        time.sleep(3)
    if not url:
        raise SystemExit("O Render ainda nao atribuiu subdominio ao servico")
    url = url.rstrip("/")
    print(f"URL do servico: {url}", flush=True)

    env_final = env_vars + [{"key": "APP_BASE_URL", "value": url}]
    pedido("PUT", f"/services/{servico_id}/env-vars", env_final)

    print("A disparar deploy...", flush=True)
    _, deploy = pedido("POST", f"/services/{servico_id}/deploys", {"clearCache": "do_not_clear"})
    deploy_id = deploy["id"]
    print(f"  deploy {deploy_id}", flush=True)

    inicio, estado = time.time(), deploy.get("status") or deploy.get("deployStatus")
    while time.time() - inicio < 1800:
        _, deploy = pedido("GET", f"/services/{servico_id}/deploys/{deploy_id}")
        novo = deploy.get("status") or deploy.get("deployStatus")
        if novo != estado:
            print(f"  deploy: {novo}", flush=True)
            estado = novo
        if estado == "live":
            break
        if estado in {"build_failed", "update_failed", "canceled", "pre_deploy_failed", "deactivated"}:
            print(f"Deploy terminou em {estado}", flush=True)
            raise SystemExit(1)
        time.sleep(15)

    if estado != "live":
        raise SystemExit("Tempo esgotado a esperar pelo deploy")

    json.dump({
        "owner_id": owner_id,
        "postgres_id": bd_id,
        "service_id": servico_id,
        "url": url,
        "deploy_id": deploy_id,
        "credenciais": {
            "admin_email": ADMIN_EMAIL,
            "admin": admin_password,
            "cliente_email": CLIENTE_EMAIL,
            "cliente": cliente_password,
        },
    }, open(ESTADO, "w"), indent=2)
    try:
        with urllib.request.urlopen(url + "/", timeout=120) as resp:
            print(f"HTTP {resp.status} na home apos o deploy", flush=True)
    except urllib.error.HTTPError as erro:
        print(f"Home respondeu HTTP {erro.code}", flush=True)

    print(f"OK: {url} (estado em {ESTADO})", flush=True)


if __name__ == "__main__":
    sys.exit(main())
