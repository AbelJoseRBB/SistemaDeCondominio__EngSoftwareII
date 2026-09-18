-- ============================================================
-- Dados de Teste - CondoManager
-- Execute APOS o schema.sql para popular o banco com dados
-- realistas para desenvolvimento e testes.
-- Usa INSERT IGNORE: seguro para executar multiplas vezes.
-- ============================================================

USE condominio_db;

-- ------------------------------------------------------------
-- Unidades (3 blocos: A, B, C — espelhando o mockup da tela)
-- ------------------------------------------------------------
INSERT IGNORE INTO unidade (bloco, numero, proprietario, situacao, telefone_contato, email_contato) VALUES
('A', '101', 'Carlos Mendes',   'OCUPADO',    '(11) 98765-1234', 'carlos.mendes@email.com'),
('A', '102', 'Fernanda Lima',   'OCUPADO',    '(11) 97654-2345', 'fernanda.lima@email.com'),
('A', '201', 'Roberto Alves',   'DESOCUPADO', '(11) 96543-3456', 'roberto.alves@email.com'),
('B', '101', 'Patricia Costa',  'ALUGUEL',    '(11) 95432-4567', 'patricia.costa@email.com'),
('B', '205', 'Marcos Souza',    'OCUPADO',    '(11) 94321-5678', 'marcos.souza@email.com'),
('C', '301', 'Ana Beatriz',     'ALUGUEL',    '(11) 93210-6789', 'ana.beatriz@email.com'),
('C', '104', 'Joao Paulo',      'OCUPADO',    '(11) 92109-7890', 'joao.paulo@email.com');

-- ------------------------------------------------------------
-- Moradores vinculados as unidades
-- (ids das unidades dependem da ordem de insercao acima)
-- ------------------------------------------------------------
INSERT IGNORE INTO morador (id_unidade, nome, cpf, telefone, email, tipo)
SELECT u.id, 'Carlos Mendes',  '123.456.789-00', '(11) 98765-1234', 'carlos.mendes@email.com',  'PROPRIETARIO'
FROM unidade u WHERE u.bloco='A' AND u.numero='101';

INSERT IGNORE INTO morador (id_unidade, nome, cpf, telefone, email, tipo)
SELECT u.id, 'Fernanda Lima',  '234.567.890-11', '(11) 97654-2345', 'fernanda.lima@email.com',  'PROPRIETARIO'
FROM unidade u WHERE u.bloco='A' AND u.numero='102';

INSERT IGNORE INTO morador (id_unidade, nome, cpf, telefone, email, tipo)
SELECT u.id, 'Patricia Costa', '345.678.901-22', '(11) 95432-4567', 'patricia.costa@email.com', 'INQUILINO'
FROM unidade u WHERE u.bloco='B' AND u.numero='101';

INSERT IGNORE INTO morador (id_unidade, nome, cpf, telefone, email, tipo)
SELECT u.id, 'Marcos Souza',   '456.789.012-33', '(11) 94321-5678', 'marcos.souza@email.com',   'PROPRIETARIO'
FROM unidade u WHERE u.bloco='B' AND u.numero='205';

-- ------------------------------------------------------------
-- Taxas condominiais pendentes
-- ------------------------------------------------------------
INSERT IGNORE INTO taxa (id_unidade, descricao, valor, vencimento, situacao)
SELECT u.id, 'Taxa de Condominio - Setembro/2026', 450.00, '2026-09-10', 'ATRASADO'
FROM unidade u WHERE u.bloco='A' AND u.numero='101';

INSERT IGNORE INTO taxa (id_unidade, descricao, valor, vencimento, situacao)
SELECT u.id, 'Taxa de Condominio - Setembro/2026', 450.00, '2026-09-10', 'PAGO'
FROM unidade u WHERE u.bloco='A' AND u.numero='102';

INSERT IGNORE INTO taxa (id_unidade, descricao, valor, vencimento, situacao)
SELECT u.id, 'Taxa de Condominio - Setembro/2026', 450.00, '2026-09-10', 'PENDENTE'
FROM unidade u WHERE u.bloco='B' AND u.numero='205';

INSERT IGNORE INTO taxa (id_unidade, descricao, valor, vencimento, situacao)
SELECT u.id, 'Fundo de Reserva - 3T/2026', 200.00, '2026-09-30', 'PENDENTE'
FROM unidade u WHERE u.bloco='B' AND u.numero='101';

-- ------------------------------------------------------------
-- Reservas de areas comuns
-- ------------------------------------------------------------
INSERT IGNORE INTO reserva (id_unidade, area_comum, data, hora_inicio, hora_fim, situacao, observacoes)
SELECT u.id, 'Salao de Festas', '2026-09-20', '18:00:00', '23:00:00', 'CONFIRMADA', 'Aniversario de 10 anos'
FROM unidade u WHERE u.bloco='A' AND u.numero='101';

INSERT IGNORE INTO reserva (id_unidade, area_comum, data, hora_inicio, hora_fim, situacao, observacoes)
SELECT u.id, 'Churrasqueira', '2026-09-21', '12:00:00', '17:00:00', 'CONFIRMADA', 'Reuniao de familia'
FROM unidade u WHERE u.bloco='B' AND u.numero='205';

-- ------------------------------------------------------------
-- Fim dos dados de teste
-- Total: 7 unidades | 4 moradores | 4 taxas | 2 reservas
-- ------------------------------------------------------------
