-- ============================================================
-- Schema do Banco de Dados - Sistema de Gestao de Condominio
-- Banco: MySQL 8.0+
-- Execute este script para criar todas as tabelas.
-- ============================================================

CREATE DATABASE IF NOT EXISTS condominio_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE condominio_db;

-- Tabela de usuarios do sistema (administradores/operadores)
CREATE TABLE IF NOT EXISTS usuario (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    nome       VARCHAR(100) NOT NULL,
    login      VARCHAR(50)  NOT NULL UNIQUE,
    senha_hash VARCHAR(255) NOT NULL,          -- Hash BCrypt
    perfil     ENUM('ADMIN', 'OPERADOR') NOT NULL DEFAULT 'OPERADOR'
);

-- Tabela de unidades (apartamentos/casas)
CREATE TABLE IF NOT EXISTS unidade (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    bloco            VARCHAR(10)  NOT NULL,
    numero           VARCHAR(10)  NOT NULL,
    proprietario     VARCHAR(100),
    situacao         ENUM('OCUPADO','DESOCUPADO','VENDA','ALUGUEL') NOT NULL DEFAULT 'DESOCUPADO',
    telefone_contato VARCHAR(20),
    email_contato    VARCHAR(100),
    UNIQUE KEY uk_bloco_numero (bloco, numero)
);

-- Tabela de moradores vinculados as unidades
CREATE TABLE IF NOT EXISTS morador (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    id_unidade INT NOT NULL,
    nome       VARCHAR(100) NOT NULL,
    cpf        VARCHAR(14)  UNIQUE,
    telefone   VARCHAR(20),
    email      VARCHAR(100),
    tipo       ENUM('PROPRIETARIO','INQUILINO','DEPENDENTE') NOT NULL,
    FOREIGN KEY (id_unidade) REFERENCES unidade(id) ON DELETE CASCADE
);

-- Tabela de taxas condominiais
CREATE TABLE IF NOT EXISTS taxa (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    id_unidade INT NOT NULL,
    descricao  VARCHAR(100) NOT NULL,
    valor      DECIMAL(10,2) NOT NULL,
    vencimento DATE NOT NULL,
    situacao   ENUM('PENDENTE','PAGO','ATRASADO') NOT NULL DEFAULT 'PENDENTE',
    FOREIGN KEY (id_unidade) REFERENCES unidade(id) ON DELETE CASCADE
);

-- Tabela de reservas de areas comuns
CREATE TABLE IF NOT EXISTS reserva (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    id_unidade  INT NOT NULL,
    area_comum  VARCHAR(100) NOT NULL,
    data        DATE NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fim    TIME NOT NULL,
    situacao    ENUM('CONFIRMADA','CANCELADA','PENDENTE') NOT NULL DEFAULT 'CONFIRMADA',
    observacoes TEXT,
    FOREIGN KEY (id_unidade) REFERENCES unidade(id) ON DELETE CASCADE
);

-- Tabela de ocorrencias e reclamacoes
CREATE TABLE IF NOT EXISTS ocorrencia (
    id                 INT AUTO_INCREMENT PRIMARY KEY,
    id_unidade         INT,                               -- Nullable (pode ser area comum)
    titulo             VARCHAR(150) NOT NULL,
    descricao          TEXT,
    categoria          ENUM('RECLAMACAO','INFORMACAO','MANUTENCAO','SEGURANCA') NOT NULL,
    situacao           ENUM('ABERTA','EM_ANDAMENTO','ENCERRADA') NOT NULL DEFAULT 'ABERTA',
    data_abertura      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_encerramento  DATETIME,
    resposta           TEXT,
    FOREIGN KEY (id_unidade) REFERENCES unidade(id) ON DELETE SET NULL
);

-- Tabela de manutencoes
CREATE TABLE IF NOT EXISTS manutencao (
    id                INT AUTO_INCREMENT PRIMARY KEY,
    id_unidade        INT,                               -- Nullable (pode ser area comum)
    descricao         VARCHAR(255) NOT NULL,
    local             VARCHAR(100),
    responsavel       VARCHAR(100),
    data_solicitacao  DATE NOT NULL,
    data_conclusao    DATE,
    situacao          ENUM('SOLICITADA','EM_ANDAMENTO','CONCLUIDA','CANCELADA') NOT NULL DEFAULT 'SOLICITADA',
    custo             DECIMAL(10,2),
    observacoes       TEXT,
    FOREIGN KEY (id_unidade) REFERENCES unidade(id) ON DELETE SET NULL
);

-- Tabela de veiculos vinculados as unidades
CREATE TABLE IF NOT EXISTS veiculo (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    id_unidade   INT NOT NULL,
    placa        VARCHAR(10) NOT NULL UNIQUE,
    modelo       VARCHAR(80),
    cor          VARCHAR(30),
    numero_vaga  VARCHAR(10),                            -- Nullable
    FOREIGN KEY (id_unidade) REFERENCES unidade(id) ON DELETE CASCADE
);

-- ============================================================
-- Dados iniciais: usuario administrador padrao
-- Senha: admin123 (hash BCrypt - TROQUE em producao!)
-- ============================================================
INSERT INTO usuario (nome, login, senha_hash, perfil)
VALUES ('Administrador', 'admin', '$2a$10$tyWUHDECdFpz9iZr2aZyJ..QWWRIzttF4yhXkCk4lU8umqjL0xxTa', 'ADMIN');
