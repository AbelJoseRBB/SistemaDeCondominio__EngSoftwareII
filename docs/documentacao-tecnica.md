# Documentação Técnica — CondoManager

## Arquitetura

O projeto segue o padrão arquitetural **MVC (Model-View-Controller)** adaptado para JavaFX:

```
View (FXML) --> Controller (Java) --> Service (Java) --> DAO (Java) --> Banco de Dados (MySQL)
```

---

## Pré-requisitos

| Ferramenta | Versão mínima | Link |
|---|---|---|
| JDK | 17 | https://adoptium.net/ |
| Maven | 3.8 | https://maven.apache.org/ |
| MySQL | 8.0 | https://dev.mysql.com/downloads/installer/ |

---

## 1. Instalando o MySQL no Windows

### Via MySQL Installer (recomendado)

1. Acesse https://dev.mysql.com/downloads/installer/ e baixe o **MySQL Installer for Windows**
2. Execute o `.msi` e escolha o tipo **"Full"** (instala o Server + Workbench)
3. Avance pelas etapas até **"Accounts and Roles"**
4. Defina a senha do usuário `root` — **anote essa senha**, ela será usada no `db.properties`
5. Conclua a instalação. O serviço MySQL será iniciado automaticamente

### Via XAMPP (alternativa)

1. Baixe em https://www.apachefriends.org/ e instale
2. Abra o **XAMPP Control Panel** e clique em **Start** ao lado de **MySQL**
3. O MySQL rodará em `localhost:3306` com usuário `root` e **senha em branco** por padrão
4. Nesse caso, deixe `db.password=` vazio no `db.properties`

---

## 2. Configurando o Banco de Dados

### 2.1 Criar o arquivo de configuração local

Copie o arquivo de exemplo e preencha com suas credenciais:

```
src/main/resources/db.properties.example  →  src/main/resources/db.properties
```

Edite o `db.properties`:

```properties
db.url=jdbc:mysql://localhost:3306/condominio_db
db.user=root
db.password=SUA_SENHA_AQUI
```

> ⚠️ O arquivo `db.properties` está no `.gitignore` e **nunca será versionado**.
> Apenas o `db.properties.example` (sem senha) vai para o repositório.

### 2.2 Criar as tabelas (schema)

Abra o **MySQL Workbench**, conecte-se ao servidor local e execute o script:

```
src/main/resources/sql/schema.sql
```

No Workbench: **File → Open SQL Script** → selecione o arquivo → clique em ⚡ *Execute*

Ou pelo terminal MySQL:

```sql
SOURCE C:/caminho/para/CondoManager/src/main/resources/sql/schema.sql;
```

> O script usa `CREATE TABLE IF NOT EXISTS` e `INSERT IGNORE`, portanto é **seguro executar múltiplas vezes**.

### 2.3 Carregar dados de teste (opcional)

Para popular o banco com dados realistas para desenvolvimento:

```
src/main/resources/sql/dados_teste.sql
```

Execute da mesma forma que o `schema.sql`. Inclui 7 unidades, moradores, taxas e reservas de exemplo.

---

## 3. Executando o Projeto

### 3.1 Via Docker (Banco de Dados Conteinerizado - Recomendado)

Para máxima flexibilidade entre membros da equipe e facilidade de movimentação do banco de dados, o MySQL 8.0 roda no Docker pré-configurado com as tabelas e dados de teste.

#### Iniciar o banco de dados:
```bash
docker compose up -d
```
ou no Windows:
```cmd
db.bat start
```

O MySQL subirá na porta `3306`, executando automaticamente:
1. `src/main/resources/sql/schema.sql` (todas as tabelas e usuário admin)
2. `src/main/resources/sql/dados_teste.sql` (unidades, moradores, taxas e reservas de exemplo)

#### Executar a aplicação (Java 17 + JavaFX 21):
```bash
mvn javafx:run
```

#### Movimentação, Backup e Restauração do Banco:
| Operação | Comando nativo Docker | Utilitário Windows (`db.bat`) |
|---|---|---|
| **Iniciar banco** | `docker compose up -d` | `db.bat start` |
| **Parar banco** | `docker compose down` | `db.bat stop` |
| **Status do banco** | `docker compose ps` | `db.bat status` |
| **Resetar banco** *(apaga e recria do zero)* | `docker compose down -v && docker compose up -d` | `db.bat reset` |
| **Exportar backup (Dump)** | `docker compose exec -T db mysqldump -u root -proot condominio_db > backup.sql` | `db.bat backup` |
| **Restaurar backup** | `docker compose exec -T db mysql -u root -proot condominio_db < backup.sql` | `db.bat restore backup.sql` |
| **Terminal MySQL (CLI)** | `docker compose exec -it db mysql -u root -proot condominio_db` | `db.bat cli` |

> 💡 **Customização de porta e senhas:** copie `.env.example` para `.env` e altere a variável `MYSQL_PORT` caso a porta 3306 já esteja em uso no seu computador.

### 3.2 Execução Local com MySQL Instalado no Sistema Operacional

```bash
mvn javafx:run
```

### Login padrão

| Campo | Valor |
|---|---|
| **Login** | `admin` |
| **Senha** | `admin123` |

> ⚠️ Altere a senha do administrador antes de usar em produção!

---

## Estrutura de Pacotes

| Pacote | Responsabilidade |
|---|---|
| `com.condomanager.model` | Classes de entidade (POJO) |
| `com.condomanager.dao` | Acesso ao banco de dados via JDBC |
| `com.condomanager.service` | Regras de negócio e validações |
| `com.condomanager.controller` | Controllers das telas FXML |
| `com.condomanager.util` | Utilitários: conexão BD, sessão, navegação |

---

## Solução de Problemas Comuns

| Erro | Causa provável | Solução |
|---|---|---|
| `Communications link failure` | MySQL não está rodando | Inicie o serviço MySQL / XAMPP |
| `Access denied for user 'root'` | Senha incorreta no `db.properties` | Verifique a senha definida na instalação |
| `Unknown database 'condominio_db'` | Schema não foi executado | Execute o `schema.sql` no Workbench |
| `db.properties not found` | Arquivo não foi criado | Copie o `.example` e renomeie |
