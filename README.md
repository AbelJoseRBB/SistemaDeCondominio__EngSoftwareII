# CondoManager - Sistema de Gestao de Condominio

Aplicacao desktop desenvolvida em Java com JavaFX para administracao interna de condominios.

## Tecnologias

- **Linguagem:** Java 17
- **Interface:** JavaFX 21 (FXML)
- **Banco de dados:** MySQL
- **Build:** Maven

## Pre-requisitos

- JDK 17 ou superior
- Maven 3.8+
- MySQL 8.0+

## Como executar

### Opção 1: Banco de Dados no Docker (Recomendado)

O banco de dados roda isolado no Docker com MySQL 8.0, populado automaticamente com o schema e dados de teste. A aplicação roda localmente para você continuar desenvolvendo a interface com agilidade.

```bash
# 1. Iniciar o banco de dados no Docker (em segundo plano):
docker compose up -d

# 2. Executar o sistema JavaFX:
mvn javafx:run
```

> **No Windows, você também pode usar o utilitário `db.bat`:**
> - `.\db.bat start` — Inicia o banco
> - `.\db.bat stop` — Para o banco
> - `.\db.bat status` — Verifica se o banco está rodando
> - `.\db.bat reset` — Reseta o banco e reaplica o schema e dados de teste
> - `.\db.bat backup` — Exporta dump SQL com os dados atuais
> - `.\db.bat restore arquivo.sql` — Restaura um dump SQL
> - `.\db.bat cli` — Abre o terminal interativo do MySQL

### Opção 2: MySQL Instalado Localmente

1. Certifique-se de que o serviço MySQL local está em execução na porta 3306
2. Execute os scripts `schema.sql` e `dados_teste.sql`
3. Execute: `mvn javafx:run`


## Estrutura do Projeto

Consulte a documentacao tecnica em `docs/documentacao-tecnica.md`.

## Equipe

- (adicione os nomes dos integrantes aqui)

## Disciplina

Engenharia de Software 2 - 2026.2
