import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Main {

    static class Jugador {
        private final long dni;
        private String nombre;
        private int edad;
        private String posicion;

        public Jugador(long dni, String nombre, int edad, String posicion) {
            this.dni = dni;
            this.nombre = nombre;
            this.edad = edad;
            this.posicion = posicion;
        }

        public long getDni() { return dni; }
        public String getNombre() { return nombre; }
        public int getEdad() { return edad; }
        public String getPosicion() { return posicion; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Jugador)) return false;
            Jugador jugador = (Jugador) o;
            return dni == jugador.dni;
        }

        @Override
        public int hashCode() {
            return Objects.hash(dni);
        }
    }

    static class Equipo {
        private final String nombre;
        private int cupoMaximo;

        public Equipo(String nombre, int cupoMaximo) {
            this.nombre = nombre;
            this.cupoMaximo = cupoMaximo;
        }

        public String getNombre() { return nombre; }
        public int getCupoMaximo() { return cupoMaximo; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Equipo)) return false;
            Equipo equipo = (Equipo) o;
            return Objects.equals(nombre, equipo.nombre);
        }

        @Override
        public int hashCode() {
            return Objects.hash(nombre);
        }
    }

    static class RegistroTorneo {
        private final Map<Long, Jugador> jugadores = new HashMap<>();
        private final Map<String, Equipo> equipos = new HashMap<>();
        private final Map<String, Set<Long>> planteles = new HashMap<>();

        public boolean registrarJugador(Jugador j) {
            if (jugadores.containsKey(j.getDni())) return false;
            jugadores.put(j.getDni(), j);
            return true;
        }

        public boolean registrarEquipo(Equipo e) {
            if (equipos.containsKey(e.getNombre())) return false;
            equipos.put(e.getNombre(), e);
            planteles.put(e.getNombre(), new HashSet<>());
            return true;
        }

        public boolean asignarJugadorAEquipo(long dni, String nombreEquipo) {
            Jugador j = jugadores.get(dni);
            Equipo e = equipos.get(nombreEquipo);
            if (j == null || e == null) return false;
            Set<Long> set = planteles.get(nombreEquipo);
            if (set.size() >= e.getCupoMaximo()) return false;
            return set.add(dni);
        }

        public List<String> equiposSinJugadores() {
            List<String> vacios = new ArrayList<>();
            for (Map.Entry<String, Set<Long>> e : planteles.entrySet()) {
                if (e.getValue().isEmpty()) vacios.add(e.getKey());
            }
            return vacios;
        }

        public Set<Long> jugadoresNoAsignados() {
            Set<Long> todos = new HashSet<>(jugadores.keySet());
            for (Set<Long> set : planteles.values()) {
                todos.removeAll(set);
            }
            return todos;
        }
    }

    private static void menu() {
        System.out.println("\n===== TORNEO =====");
        System.out.println("1) Registrar jugador");
        System.out.println("2) Registrar equipo");
        System.out.println("3) Asignar jugador a equipo");
        System.out.println("4) Mostrar equipos sin jugadores");
        System.out.println("5) Mostrar jugadores sin asignar");
        System.out.println("0) Salir");
    }

    public static void main(String[] args) {
        RegistroTorneo rt = new RegistroTorneo();
        Scanner sc = new Scanner(System.in);

        while (true) {
            menu();
            String op = sc.nextLine().trim();
            switch (op) {
                case "1" -> {
                    System.out.print("DNI: ");
                    long dni = Long.parseLong(sc.nextLine().trim());
                    System.out.print("Nombre: ");
                    String nom = sc.nextLine().trim();
                    System.out.print("Edad: ");
                    int edad = Integer.parseInt(sc.nextLine().trim());
                    System.out.print("Posición: ");
                    String pos = sc.nextLine().trim();
                    System.out.println(rt.registrarJugador(new Jugador(dni, nom, edad, pos)) ? "Jugador registrado." : "Duplicado.");
                }
                case "2" -> {
                    System.out.print("Nombre del equipo: ");
                    String nom = sc.nextLine().trim();
                    System.out.print("Cupo máximo: ");
                    int cupo = Integer.parseInt(sc.nextLine().trim());
                    System.out.println(rt.registrarEquipo(new Equipo(nom, cupo)) ? "Equipo registrado." : "Duplicado.");
                }
                case "3" -> {
                    System.out.print("DNI del jugador: ");
                    long dni = Long.parseLong(sc.nextLine().trim());
                    System.out.print("Nombre del equipo: ");
                    String nom = sc.nextLine().trim();
                    System.out.println(rt.asignarJugadorAEquipo(dni, nom) ? "Asignado." : "No se pudo asignar.");
                }
                case "4" -> System.out.println("Equipos sin jugadores: " + rt.equiposSinJugadores());
                case "5" -> System.out.println("Jugadores sin asignar: " + rt.jugadoresNoAsignados());
                case "0" -> { System.out.println("Adiós!"); return; }
                default -> System.out.println("Opción inválida.");
            }
        }
    }
}
