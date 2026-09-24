package com.condomanager;

import com.condomanager.controller.veiculo.*;
import com.condomanager.dao.*;
import com.condomanager.model.*;
import com.condomanager.service.VeiculoService;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

class VeiculoViewsTest {
    @BeforeAll static void iniciar() {
        try { Platform.startup(()->{}); } catch(IllegalStateException ignored) { }
    }
    private Veiculo veiculo() {
        Veiculo v=new Veiculo(); v.setId(1); v.setIdUnidade(1); v.setIdProprietario(1);
        v.setPlaca("ABC-1D23"); v.setModelo("Onix"); v.setMarca("Chevrolet"); v.setCor("Prata");
        v.setNumeroVaga("A-01"); v.setUnidade("A-101"); v.setProprietario("Carlos Mendes"); return v;
    }
    private VeiculoService service() { return new VeiculoService(new VeiculoDAO() {
        @Override public List<Veiculo> listarTodos() { return List.of(veiculo()); }
        @Override public int[] ocupacao(int unidade,int ignorar) { return new int[]{ignorar==0?2:1,2}; }
    }); }
    @Test void listaBuscaEFormularioCarregam() throws Exception {
        CompletableFuture<Void> futuro=new CompletableFuture<>();
        Platform.runLater(()-> {
            try {
                FXMLLoader lista=new FXMLLoader(getClass().getResource("/fxml/veiculo/VeiculoList.fxml"));
                lista.setControllerFactory(c -> new VeiculoListController(service()));
                Parent root=lista.load(); new Scene(root,1250,720); root.applyCss(); root.layout();
                TableView<?> tabela=(TableView<?>)root.lookup("#tabela");
                assertEquals(1,tabela.getItems().size());
                TextField busca=(TextField)root.lookup("#txtBusca"); busca.setText("carlos"); assertEquals(1,tabela.getItems().size());
                busca.setText("inexistente"); assertEquals(0,tabela.getItems().size());
                busca.setText(""); snapshot(root,"target/veiculos-lista.png");
                Unidade u=new Unidade(); u.setId(1); u.setBloco("A"); u.setNumero("101");
                Morador m=new Morador(); m.setId(1); m.setNome("Carlos Mendes"); m.setSituacao("ATIVO");
                FXMLLoader form=new FXMLLoader(getClass().getResource("/fxml/veiculo/VeiculoForm.fxml"));
                form.setControllerFactory(c -> new VeiculoFormController(service(),new UnidadeDAO() {
                    @Override public List<Unidade> listarTodas() { return List.of(u); }
                },new MoradorDAO() { @Override public List<Morador> listarPorUnidade(int id) { return List.of(m); } }));
                Parent modal=form.load(); new Scene(modal); modal.applyCss(); modal.layout();
                ComboBox<?> unidade=(ComboBox<?>)modal.lookup("#cbUnidade"); unidade.getSelectionModel().selectFirst();
                assertTrue(((Button)modal.lookup("#btnSalvar")).isDisabled(),"Nova inclusão bloqueada no limite");
                VeiculoFormController controller=form.getController();
                // Reabrir seleção força o recálculo para edição do próprio veículo.
                unidade.getSelectionModel().clearSelection(); controller.configurar(veiculo(),()->{});
                assertFalse(((Button)modal.lookup("#btnSalvar")).isDisabled(),"Edição desconsidera o próprio veículo");
                assertEquals(m,((ComboBox<?>)modal.lookup("#cbProprietario")).getValue());
                modal.applyCss(); modal.layout(); snapshot(modal,"target/veiculos-formulario.png");
                futuro.complete(null);
            } catch(Throwable e) { futuro.completeExceptionally(e); }
        });
        futuro.get(15,TimeUnit.SECONDS);
    }
    private void snapshot(Parent root,String path) throws Exception {
        var image=root.snapshot(null,null);
        var output=new java.awt.image.BufferedImage((int)image.getWidth(),(int)image.getHeight(),java.awt.image.BufferedImage.TYPE_INT_ARGB);
        for(int y=0;y<output.getHeight();y++) for(int x=0;x<output.getWidth();x++) output.setRGB(x,y,image.getPixelReader().getArgb(x,y));
        javax.imageio.ImageIO.write(output,"png",new java.io.File(path));
    }
}
