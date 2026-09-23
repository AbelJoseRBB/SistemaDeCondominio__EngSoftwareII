-- Executar uma unica vez em bancos criados antes do modulo de veiculos.
-- Revise placas/vagas duplicadas antes de aplicar; nenhum registro e removido.
USE condominio_db;
ALTER TABLE unidade ADD COLUMN limite_veiculos INT NOT NULL DEFAULT 2 CHECK (limite_veiculos >= 0);
ALTER TABLE veiculo
    ADD COLUMN marca VARCHAR(80),
    ADD COLUMN id_proprietario INT,
    ADD CONSTRAINT fk_veiculo_proprietario FOREIGN KEY (id_proprietario) REFERENCES morador(id),
    ADD COLUMN placa_normalizada VARCHAR(10) GENERATED ALWAYS AS (UPPER(REPLACE(TRIM(placa), '-', ''))) STORED,
    ADD COLUMN vaga_normalizada VARCHAR(10) GENERATED ALWAYS AS (NULLIF(UPPER(TRIM(numero_vaga)), '')) STORED,
    ADD UNIQUE KEY uk_veiculo_placa_normalizada (placa_normalizada),
    ADD UNIQUE KEY uk_veiculo_vaga (vaga_normalizada);
-- Veiculos legados permanecem listados; selecione o proprietario ao edita-los.
