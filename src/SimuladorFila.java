import java.util.LinkedList;
import java.util.Queue;

public class SimuladorFila {
    // Configurações do LCG (Gerador de números pseudo-aleatórios)
    private static final long M = (long) Math.pow(2, 31);
    private static final long a = 1664525;
    private static final long c = 1013904223;
    private long seed;

    // Parâmetros da simulação
    private int capacidadeFila;       // Tamanho máximo da fila (ex: 5)
    private int numServidores;        // Número de servidores (ex: 1 ou 2)
    private double minChegada;        // Tempo mínimo entre chegadas (ex: 2)
    private double maxChegada;        // Tempo máximo entre chegadas (ex: 5)
    private double minAtendimento;    // Tempo mínimo de atendimento (ex: 3)
    private double maxAtendimento;    // Tempo máximo de atendimento (ex: 5)

    // Estado da simulação
    private Queue<Double> fila;
    private double tempoGlobal;
    private double proximaChegada;
    private double[] proximaSaida;
    private int clientesPerdidos;
    private double[] temposEstado;    // Tempos acumulados para cada estado da fila (0..capacidadeFila)
    private int totalAleatorios;
    private int aleatoriosUsados;

    public SimuladorFila(long seed, int capacidadeFila, int numServidores, 
                         double minChegada, double maxChegada, 
                         double minAtendimento, double maxAtendimento) {
        this.seed = seed;
        this.capacidadeFila = capacidadeFila;
        this.numServidores = numServidores;
        this.minChegada = minChegada;
        this.maxChegada = maxChegada;
        this.minAtendimento = minAtendimento;
        this.maxAtendimento = maxAtendimento;

        this.fila = new LinkedList<>();
        this.tempoGlobal = 0;
        this.proximaChegada = 2.0; // Primeira chegada no tempo 2.0
        this.proximaSaida = new double[numServidores];
        for (int i = 0; i < numServidores; i++) {
            proximaSaida[i] = Double.POSITIVE_INFINITY; // Inicialmente, nenhum atendimento
        }
        this.clientesPerdidos = 0;
        this.temposEstado = new double[capacidadeFila + 1]; // Estados 0..capacidadeFila
        this.totalAleatorios = 100000;
        this.aleatoriosUsados = 0;
    }

    // Gera um número pseudo-aleatório no intervalo [0, 1)
    private double nextRandom() {
        seed = (a * seed + c) % M;
        aleatoriosUsados++;
        return (double) seed / M;
    }

    // Gera um tempo entre chegadas ou atendimento baseado nos intervalos
    private double gerarTempo(double min, double max) {
        return min + (max - min) * nextRandom();
    }

    // Executa a simulação
    public void simular() {
        while (aleatoriosUsados < totalAleatorios) {
            // Determina o próximo evento (chegada ou saída mais próxima)
            double tempoProximoEvento = proximaChegada;
            int servidorProximaSaida = -1;

            for (int i = 0; i < numServidores; i++) {
                if (proximaSaida[i] < tempoProximoEvento) {
                    tempoProximoEvento = proximaSaida[i];
                    servidorProximaSaida = i;
                }
            }

            // Atualiza o tempo global e os tempos acumulados para o estado atual da fila
            double tempoDecorrido = tempoProximoEvento - tempoGlobal;
            int estadoAtual = fila.size();
            temposEstado[estadoAtual] += tempoDecorrido;
            tempoGlobal = tempoProximoEvento;

            // Processa o evento (chegada ou saída)
            if (servidorProximaSaida == -1) {
                // Evento de CHEGADA
                if (fila.size() < capacidadeFila) {
                    fila.add(tempoGlobal);
                    // Agenda próxima chegada
                    proximaChegada = tempoGlobal + gerarTempo(minChegada, maxChegada);
                    // Se há servidor livre, inicia atendimento
                    for (int i = 0; i < numServidores; i++) {
                        if (proximaSaida[i] == Double.POSITIVE_INFINITY) {
                            proximaSaida[i] = tempoGlobal + gerarTempo(minAtendimento, maxAtendimento);
                            break;
                        }
                    }
                } else {
                    clientesPerdidos++;
                    proximaChegada = tempoGlobal + gerarTempo(minChegada, maxChegada);
                }
            } else {
                // Evento de SAÍDA
                fila.poll();
                if (!fila.isEmpty()) {
                    proximaSaida[servidorProximaSaida] = tempoGlobal + gerarTempo(minAtendimento, maxAtendimento);
                } else {
                    proximaSaida[servidorProximaSaida] = Double.POSITIVE_INFINITY;
                }
            }
        }
    }

    // Imprime os resultados da simulação
    public void imprimirResultados() {
        System.out.println("\n=== RESULTADOS DA SIMULAÇÃO ===");
        System.out.printf("Configuração: G/G/%d/%d\n", numServidores, capacidadeFila);
        System.out.printf("Chegadas: %.2f..%.2f | Atendimento: %.2f..%.2f\n", 
                          minChegada, maxChegada, minAtendimento, maxAtendimento);
        System.out.printf("Tempo global: %.2f\n", tempoGlobal);
        System.out.printf("Clientes perdidos: %d\n", clientesPerdidos);
        System.out.println("\nDistribuição de probabilidades dos estados da fila:");

        for (int i = 0; i < temposEstado.length; i++) {
            double probabilidade = (temposEstado[i] / tempoGlobal) * 100;
            System.out.printf("Estado %d: %.2f%% (Tempo acumulado: %.2f)\n", 
                             i, probabilidade, temposEstado[i]);
        }
    }

    public static void main(String[] args) {
        // Simulação G/G/1/5
        SimuladorFila simulador1 = new SimuladorFila(42, 5, 1, 2.0, 5.0, 3.0, 5.0);
        simulador1.simular();
        simulador1.imprimirResultados();

        // Simulação G/G/2/5
        SimuladorFila simulador2 = new SimuladorFila(42, 5, 2, 2.0, 5.0, 3.0, 5.0);
        simulador2.simular();
        simulador2.imprimirResultados();
    }
}