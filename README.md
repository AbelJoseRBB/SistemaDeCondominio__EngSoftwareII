# 🏢 CondoManager — Sistema de Gestão de Condomínio

O **CondoManager** é uma aplicação desktop desenvolvida em Java com JavaFX, voltada para a administração interna de condomínios.

O sistema tem como objetivo centralizar e facilitar o gerenciamento de unidades, moradores, taxas condominiais, reservas de áreas comuns, ocorrências, manutenções e veículos.

Desenvolvido como projeto acadêmico da disciplina de **Engenharia de Software II**, o CondoManager utiliza uma interface gráfica desktop e um banco de dados relacional para garantir a persistência e a integridade das informações.

---

## 📋 Funcionalidades

O sistema oferece as seguintes funcionalidades:

| Módulo | Descrição |
|---|---|
| Autenticação | Controle de acesso ao sistema por meio de login e senha. |
| Gestão de Usuários | Cadastro e gerenciamento de usuários administrativos. |
| Gestão de Unidades | Cadastro, consulta, edição e exclusão de unidades condominiais. |
| Gestão de Moradores | Cadastro e gerenciamento de moradores vinculados às unidades. |
| Gestão de Taxas | Registro e acompanhamento de taxas condominiais e situações de pagamento. |
| Reservas de Áreas Comuns | Gerenciamento de reservas com validação de datas, horários e conflitos. |
| Registro de Ocorrências | Cadastro e acompanhamento de reclamações, observações e ocorrências. |
| Gestão de Manutenções | Registro e acompanhamento de serviços de manutenção, responsáveis, prazos e custos. |
| Gestão de Veículos | Cadastro de veículos vinculados às unidades, com controle de vagas e validação de duplicidades. |
| Relatórios | Geração de relatórios administrativos com filtros e possibilidade de exportação para Excel. |

---

## 🛠️ Tecnologias Utilizadas

| Tecnologia | Utilização |
|---|---|
| Java 17 | Linguagem de programação principal. |
| JavaFX 21 | Desenvolvimento da interface gráfica desktop. |
| FXML | Estruturação das interfaces gráficas. |
| CSS | Estilização dos componentes visuais. |
| MySQL 8.0 | Banco de dados relacional. |
| JDBC | Comunicação entre a aplicação Java e o banco de dados. |
| Maven | Gerenciamento de dependências e execução do projeto. |
| Docker | Conteinerização do banco de dados para facilitar a configuração do ambiente. |

---

## 🏗️ Arquitetura do Sistema

O CondoManager utiliza o padrão arquitetural **MVC (Model-View-Controller)**, adaptado para JavaFX, com camadas adicionais para regras de negócio e persistência de dados.

O fluxo principal da aplicação segue a estrutura:

```text
┌──────────────────────┐
│     View (FXML)      │
│   Interface Gráfica  │
└──────────┬───────────┘
           │
           ▼
┌──────────────────────┐
│      Controller      │
│ Controle das Telas   │
└──────────┬───────────┘
           │
           ▼
┌──────────────────────┐
│       Service        │
│  Regras de Negócio   │
└──────────┬───────────┘
           │
           ▼
┌──────────────────────┐
│         DAO          │
│ Persistência de Dados│
└──────────┬───────────┘
           │
           ▼
┌──────────────────────┐
│        MySQL         │
│    Banco de Dados    │
└──────────────────────┘
```

A separação em camadas facilita a manutenção, a organização e a evolução do sistema.

Para mais informações sobre a arquitetura e a estrutura de pacotes, consulte a [Documentação Técnica](docs/documentacao-tecnica.md).

---

## ⚙️ Pré-requisitos

Para executar o projeto, é necessário possuir as seguintes ferramentas:

| Ferramenta | Versão mínima | Download |
|---|---|---|
| JDK | 17 | [Eclipse Temurin](https://adoptium.net/) |
| Maven | 3.8 | [Apache Maven](https://maven.apache.org/download.cgi) |

Além das ferramentas acima, é necessário utilizar uma das seguintes opções para o banco de dados:

- **Docker:** para executar o MySQL em um contêiner.
- **MySQL 8.0+:** para executar o banco de dados diretamente no sistema operacional.

> **Observação:** O Docker é opcional. A aplicação JavaFX é executada localmente em ambas as configurações.

---

## 🚀 Como Executar

### 1. Clonar o repositório

Clone o repositório do projeto:

```bash
git clone https://github.com/AbelJoseRBB/SistemaDeCondominio__EngSoftwareII.git
```

Acesse o diretório do projeto:

```bash
cd SistemaDeCondominio__EngSoftwareII
```

### 2. Configurar o banco de dados

Escolha uma das opções de execução abaixo.

---

### Opção 1: Banco de Dados com Docker (Recomendado)

Esta opção utiliza o Docker para executar o MySQL 8.0 em um ambiente isolado, facilitando a configuração e a padronização do banco de dados entre os integrantes da equipe.

**Pré-requisito adicional:** [Docker Desktop](https://www.docker.com/products/docker-desktop/).

#### Iniciar o banco de dados

Na raiz do projeto, execute:

```bash
docker compose up -d
```

O Docker iniciará o MySQL e, na primeira inicialização de um banco vazio, executará automaticamente os scripts de criação das tabelas e inserção dos dados de teste.

#### Configurar a conexão

Caso ainda não exista, crie o arquivo:

```text
src/main/resources/db.properties
```

Utilize o arquivo `db.properties.example` como referência e configure as credenciais de acordo com o ambiente Docker.

#### Executar a aplicação

Com o banco de dados em execução, utilize:

```bash
mvn javafx:run
```

#### Gerenciamento do banco de dados

No Windows, o projeto disponibiliza o utilitário `db.bat` para facilitar o gerenciamento do banco.

| Comando | Descrição |
|---|---|
| `.\db.bat start` | Inicia o banco de dados. |
| `.\db.bat stop` | Para o banco de dados. |
| `.\db.bat status` | Verifica o estado do banco. |
| `.\db.bat reset` | Apaga os dados e recria o banco. |
| `.\db.bat backup` | Exporta um backup SQL. |
| `.\db.bat restore arquivo.sql` | Restaura um backup SQL. |
| `.\db.bat cli` | Abre o terminal interativo do MySQL. |

> **Atenção:** O comando `reset` remove os dados existentes e recria o banco de dados. Utilize-o somente quando desejar reinicializar o ambiente.

---

### Opção 2: MySQL Instalado Localmente

Esta opção permite executar o sistema utilizando uma instalação local do MySQL, sem necessidade de Docker.

#### 1. Instalar o MySQL

Instale o [MySQL 8.0 ou superior](https://dev.mysql.com/downloads/installer/).

Durante a instalação, configure o usuário administrador e sua respectiva senha.

Certifique-se de que o serviço MySQL esteja em execução.

#### 2. Configurar a conexão

Copie o arquivo:

```text
src/main/resources/db.properties.example
```

Renomeie a cópia para:

```text
db.properties
```

Configure os dados de conexão:

```properties
db.url=jdbc:mysql://localhost:3306/condominio_db
db.user=root
db.password=SUA_SENHA_AQUI
```

Substitua `SUA_SENHA_AQUI` pela senha configurada durante a instalação do MySQL.

> **Importante:** O arquivo `db.properties` contém credenciais locais e não deve ser enviado ao repositório. Ele está incluído no `.gitignore`.

#### 3. Criar o banco de dados

Abra o MySQL Workbench e execute o script:

```text
src/main/resources/sql/schema.sql
```

Esse script é responsável pela criação da estrutura do banco de dados.

Opcionalmente, execute:

```text
src/main/resources/sql/dados_teste.sql
```

Esse segundo script insere registros de exemplo para facilitar os testes e o desenvolvimento da aplicação.

#### 4. Executar a aplicação

Na raiz do projeto, execute:

```bash
mvn javafx:run
```

A interface gráfica do CondoManager será iniciada.

---

## 🔐 Acesso ao Sistema

Para acessar o sistema em um ambiente de desenvolvimento com os dados iniciais, utilize:

| Campo | Credencial |
|---|---|
| Usuário | `admin` |
| Senha | `admin123` |

> **Atenção:** As credenciais acima são destinadas exclusivamente ao ambiente de desenvolvimento e testes. A senha padrão deve ser alterada antes de qualquer utilização com dados reais.

---

## 📁 Estrutura do Projeto

A aplicação está organizada em pacotes de acordo com as responsabilidades de cada camada.

```text
src/
└── main/
    ├── java/
    │   └── com/
    │       └── condomanager/
    │           ├── controller/
    │           ├── dao/
    │           ├── model/
    │           ├── service/
    │           └── util/
    │
    └── resources/
        ├── css/
        ├── sql/
        │   ├── schema.sql
        │   ├── dados_teste.sql
        │   └── migracao_veiculos.sql
        │
        ├── db.properties.example
        └── db.properties
```

### Responsabilidades dos pacotes

| Pacote | Responsabilidade |
|---|---|
| `model` | Representação das entidades do sistema. |
| `dao` | Operações de acesso e persistência no banco de dados. |
| `service` | Implementação das regras de negócio e validações. |
| `controller` | Controle das interfaces e interação com o usuário. |
| `util` | Classes utilitárias, conexão com o banco, sessão e navegação. |

---

## 📚 Documentação

O projeto possui documentação complementar para auxiliar na instalação, configuração, utilização e manutenção do sistema.

- [Documentação Técnica](docs/documentacao-tecnica.md): arquitetura, estrutura de pacotes, configuração do ambiente, gerenciamento do banco de dados e procedimentos de manutenção.

- [Documento de Requisitos](docs/DocRequisitos.pdf): especificação dos requisitos funcionais e não funcionais, casos de uso e fluxos de execução do sistema.

> O documento de requisitos apresenta as funcionalidades previstas e as regras de negócio que orientaram o desenvolvimento do CondoManager.

---

## 👥 Equipe de Desenvolvimento

Projeto desenvolvido pelos discentes do curso de Engenharia da Computação da Universidade Federal do Vale do São Francisco (UNIVASF).

- Abel José Rocha Barros Bezerra
- Arthur Guilherme Araújo de Oliveira
- Guilherme Emetério Santos Lima
- João Victor de Oliveira
- Marcos Antônio de Lima Filho

---

## 🎓 Informações Acadêmicas

**Instituição:** Universidade Federal do Vale do São Francisco (UNIVASF)

**Curso:** Engenharia da Computação

**Disciplina:** Engenharia de Software II

**Professor:** Dr. Ricardo Argenton Ramos

**Semestre:** 2026.2

**Local:** Petrolina — PE

---

## 📄 Finalidade do Projeto

O CondoManager foi desenvolvido como projeto acadêmico, com o objetivo de aplicar conceitos de Engenharia de Software, incluindo levantamento e especificação de requisitos, modelagem de sistemas, arquitetura de software, desenvolvimento orientado a objetos, persistência de dados e trabalho colaborativo.

A aplicação destina-se ao gerenciamento administrativo local de condomínios, sem dependência de serviços web ou computação em nuvem para suas funcionalidades principais.