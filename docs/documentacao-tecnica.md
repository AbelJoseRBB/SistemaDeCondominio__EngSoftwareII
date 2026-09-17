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
