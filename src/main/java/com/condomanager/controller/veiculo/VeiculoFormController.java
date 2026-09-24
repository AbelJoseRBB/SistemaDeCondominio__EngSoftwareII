package com.condomanager.controller.veiculo;

import com.condomanager.dao.MoradorDAO;
import com.condomanager.dao.UnidadeDAO;
import com.condomanager.model.*;
import com.condomanager.service.VeiculoService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import java.util.function.Function;

public class VeiculoFormController {
    @FXML private Label lblTitulo, lblOcupacao, lblErro;
    @FXML private TextField txtPlaca, txtModelo, txtMarca, txtCor, txtVaga;
    @FXML private ComboBox<Unidade> cbUnidade;
    @FXML private ComboBox<Morador> cbProprietario;
    @FXML private Button btnSalvar;
    private final VeiculoService service;
    private final UnidadeDAO unidades;
    private final MoradorDAO moradores;
    private int id;
    private Runnable aoSalvar=() -> {};
    public VeiculoFormController() { this(new VeiculoService(),new UnidadeDAO(),new MoradorDAO()); }
    public VeiculoFormController(VeiculoService service,UnidadeDAO unidades,MoradorDAO moradores) {
        this.service=service; this.unidades=unidades; this.moradores=moradores;
    }
    @FXML public void initialize() {
        cbUnidade.setConverter(converter(u -> u.getBloco()+"-"+u.getNumero()));
        cbProprietario.setConverter(converter(Morador::getNome));
        cbUnidade.valueProperty().addListener((o,a,b) -> atualizarUnidade());
        try {
            cbUnidade.getItems().setAll(unidades.listarTodas());
            if(cbUnidade.getItems().isEmpty()) erro("Cadastre uma unidade antes de adicionar veículos.");
        } catch(RuntimeException e) { erro("Não foi possível carregar as unidades. Verifique o banco de dados."); btnSalvar.setDisable(true); }
    }
    private <T> StringConverter<T> converter(Function<T,String> nome) {
        return new StringConverter<>() {
            public String toString(T item) { return item==null?"":nome.apply(item); }
            public T fromString(String texto) { return null; }
        };
    }
    public void configurar(Veiculo v,Runnable callback) {
        aoSalvar=callback;
        if(v==null) return;
        id=v.getId(); lblTitulo.setText("Editar Veículo");
        txtPlaca.setText(v.getPlaca()); txtModelo.setText(v.getModelo()); txtMarca.setText(v.getMarca());
        txtCor.setText(v.getCor()); txtVaga.setText(v.getNumeroVaga());
        cbUnidade.getItems().stream().filter(u -> u.getId()==v.getIdUnidade()).findFirst().ifPresent(cbUnidade::setValue);
        cbProprietario.getItems().stream().filter(m -> m.getId()==v.getIdProprietario()).findFirst().ifPresent(cbProprietario::setValue);
    }
    private void atualizarUnidade() {
        cbProprietario.getSelectionModel().clearSelection(); cbProprietario.getItems().clear();
        Unidade u=cbUnidade.getValue();
        if(u==null) { lblOcupacao.setText(""); return; }
        try {
            cbProprietario.getItems().setAll(moradores.listarPorUnidade(u.getId()).stream().filter(m -> "ATIVO".equals(m.getSituacao())).toList());
            int[] vagas=service.ocupacao(u.getId(),id);
            boolean cheio=vagas[0]>=vagas[1];
            lblOcupacao.setText(cheio?"Limite máximo atingido: "+vagas[1]+" veículo(s)/vaga(s).":vagas[0]+" de "+vagas[1]+" vaga(s) ocupada(s) por outros veículos.");
            btnSalvar.setDisable(cheio || cbProprietario.getItems().isEmpty());
            erro(cbProprietario.getItems().isEmpty()?"Cadastre um morador ativo nesta unidade para selecionar o proprietário.":"");
        } catch(RuntimeException e) { erro("Não foi possível carregar proprietários e vagas. Verifique o banco de dados."); btnSalvar.setDisable(true); }
    }
    @FXML private void onSalvar() {
        Veiculo v=new Veiculo(); v.setId(id); v.setPlaca(txtPlaca.getText()); v.setModelo(txtModelo.getText());
        v.setMarca(txtMarca.getText()); v.setCor(txtCor.getText()); v.setNumeroVaga(txtVaga.getText());
        if(cbUnidade.getValue()!=null) v.setIdUnidade(cbUnidade.getValue().getId());
        if(cbProprietario.getValue()!=null) v.setIdProprietario(cbProprietario.getValue().getId());
        try { service.salvar(v); }
        catch(RuntimeException e) { erro(e.getMessage()); return; }
        onCancelar(); aoSalvar.run();
    }
    @FXML private void onCancelar() { ((Stage)lblTitulo.getScene().getWindow()).close(); }
    private void erro(String texto) { lblErro.setText(texto); lblErro.setManaged(!texto.isEmpty()); lblErro.setVisible(!texto.isEmpty()); }
}
