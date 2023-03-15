import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;


public class Consultas {
    private LeArquivos leArquivos;
    private ArrayList<RegistroDoTempo> registros;
    
    public Consultas(){
       leArquivos = new LeArquivos();
       leArquivos.carregaDados();
       registros = leArquivos.cloneRegistros();
       
    }
    


       

    public List<String> datasEmQueChouveuMaisDe(double milimetros){

        return registros
            .stream()
            .filter(r->r.getPrecipitacaoMaxima() > milimetros)
            .map(r->r.getDia()+"/"+r.getMes()+"/"+r.getAno())
            .toList();
    }

    public String diaQueMaisChoveuNoAno(int ano){
        RegistroDoTempo registro = registros
        .stream()
        .filter(reg->reg.getAno() == ano)
        .max(Comparator.comparing(RegistroDoTempo::getPrecipitacaoMaxima))
        .orElseThrow(IllegalArgumentException::new);
        String resp = registro.getDia()+"/"+registro.getMes()+"/"+registro.getAno()+", "+registro.getPrecipitacaoMaxima();
        return resp;
    }
}
