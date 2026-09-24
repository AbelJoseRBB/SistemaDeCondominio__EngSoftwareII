package com.condomanager.service;

import com.condomanager.dao.UsuarioDAO;
import com.condomanager.model.Usuario;
import org.mindrot.jbcrypt.BCrypt;

import java.util.regex.Pattern;

/**
 * Servico de gestao de usuarios e perfis de acesso.
 * Responsavel por validacoes de campos obrigatorios, unicidade de email/login
 * e hashing de senhas.
 */
public class UsuarioService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    );

    private final UsuarioDAO usuarioDAO;

    public UsuarioService() {
        this(new UsuarioDAO());
    }

    public UsuarioService(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    /**
     * Cadastra um novo usuario no sistema com perfil default OPERADOR.
     */
    public Usuario cadastrar(String nome, String login, String senha, String email) {
        return cadastrar(nome, login, senha, email, "OPERADOR");
    }

    /**
     * Valida os dados, verifica unicidade de login e email, gera o hash BCrypt da senha
     * e persiste o usuario no banco de dados.
     *
     * @param nome   Nome completo do usuario
     * @param login  Nome de usuario / login para autenticacao
     * @param senha  Senha em texto plano
     * @param email  E-mail do usuario (unico)
     * @param perfil Perfil de acesso (ex: ADMIN, OPERADOR)
     * @return Usuario cadastrado
     * @throws IllegalArgumentException se alguma validacao de negocio falhar
     */
    public Usuario cadastrar(String nome, String login, String senha, String email, String perfil) {
        // 1. Validacao de preenchimento dos campos obrigatorios
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("O campo Nome Completo e obrigatorio.");
        }
        if (login == null || login.trim().isEmpty()) {
            throw new IllegalArgumentException("O campo Usuario e obrigatorio.");
        }
        if (senha == null || senha.isEmpty()) {
            throw new IllegalArgumentException("O campo Senha e obrigatorio.");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("O campo E-mail e obrigatorio.");
        }

        String nomeTrimmed = nome.trim();
        String loginTrimmed = login.trim();
        String emailTrimmed = email.trim();

        if (perfil == null || perfil.trim().isEmpty()) {
            perfil = "OPERADOR";
        } else {
            perfil = perfil.trim().toUpperCase();
        }

        // 2. Validacao de formato de e-mail
        if (!EMAIL_PATTERN.matcher(emailTrimmed).matches()) {
            throw new IllegalArgumentException("Formato de e-mail invalido. Exemplo: usuario@dominio.com");
        }

        // 3. Validacao de unicidade do e-mail cadastrado
        if (usuarioDAO.existeEmail(emailTrimmed)) {
            throw new IllegalArgumentException("O e-mail informado ja esta cadastrado no sistema.");
        }

        // 4. Validacao de unicidade do login cadastrado
        if (usuarioDAO.existeLogin(loginTrimmed)) {
            throw new IllegalArgumentException("O nome de usuario informado ja esta em uso.");
        }

        // 5. Criptografia da senha com BCrypt
        String senhaHash = BCrypt.hashpw(senha, BCrypt.gensalt());

        Usuario novoUsuario = new Usuario(0, nomeTrimmed, loginTrimmed, emailTrimmed, senhaHash, perfil);

        // 6. Salvar no banco de dados
        usuarioDAO.salvar(novoUsuario);

        return novoUsuario;
    }

    public Usuario buscarPorLogin(String login) {
        return usuarioDAO.buscarPorLogin(login);
    }

    public Usuario buscarPorEmail(String email) {
        return usuarioDAO.buscarPorEmail(email);
    }
}