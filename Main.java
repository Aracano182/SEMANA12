import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Main {

    // =======================
    // ======= MODELOS =======
    // =======================

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

        public void setNombre(String nombre) { this.nombre = nombre; }
        public void setEdad(int edad) { this.edad = edad; }
        public void setPosicion(String posicion) { this.posicion = posicion; }

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
        public void setCupoMaximo(int cupoMaximo) { this.cupoMaximo = cupoMaximo; }

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

    // =================================
    // ======= LÓGICA DE NEGOCIO =======
    // =================================

    static class RegistroTorneo {
        // Jugadores por DNI
        private final Map<Long, Jugador> jugadores = new HashMap<>();
        // Equipos por nombre
        private final Map<String, Equipo> equipos = new HashMap<>();
        // Planteles: nombreEquipo -> set de DNIs
        private final Map<String, Set<Long>> planteles = new HashMap<>();

        // ---------- CRUD BÁSICO ----------
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

        public boolean eliminarJugador(long dni) {
            if (!jugadores.containsKey(dni)) return false;
            for (Set<Long> set : planteles.values()) set.remove(dni);
            jugadores.remove(dni);
            return true;
        }

        public boolean eliminarEquipo(String nombreEquipo) {
            if (!equipos.containsKey(nombreEquipo)) return false;
            equipos.remove(nombreEquipo);
            planteles.remove(nombreEquipo);
            return true;
        }

        // ---------- Asignaciones ----------
        public boolean asignarJugadorAEquipo(long dni, String nombreEquipo) {
            Jugador j = jugadores.get(dni);
            Equipo e = equipos.get(nombreEquipo);
            if (j == null || e == null) return false;
            Set<Long> set = planteles.get(nombreEquipo);
            if (set.size() >= e.getCupoMaximo()) return false;
            return set.add(dni); // HashSet garantiza no duplicados
        }

        public boolean retirarJugadorDeEquipo(long dni, String nombreEquipo) {
            Set<Long> set = planteles.get(nombreEquipo);
            if (set == null) return false;
            return set.remove(dni);
        }

        // ---------- Consultas ----------
        public List<String> equiposSinJugadores() {
            List<String> vacios = new ArrayList<>();
            for (Map.Entry<String, Set<Long>> e : planteles.entrySet()) {
                if (e.getValue().isEmpty()) vacios.add(e.getKey());
            }
            Collections.sort(vacios);
            return vacios;
        }

        public Set<Long> jugadoresNoAsignados() {
            Set<Long> todos = new HashSet<>(jugadores.keySet());
            for (Set<Long> set : planteles.values()) {
                todos.removeAll(set);
            }
            return todos;
        }

        public Set<Long> plantel(String nombreEquipo) {
            return new HashSet<>(planteles.getOrDefault(nombreEquipo, Collections.emptySet()));
        }

        public Set<Long> unionPlanteles(String equipoA, String equipoB) {
            Set<Long> a = plantel(equipoA);
            Set<Long> b = plantel(equipoB);
            a.addAll(b);
            return a;
        }

        public Set<Long> interseccionPlanteles(String equipoA, String equipoB) {
            Set<Long> a = plantel(equipoA);
            Set<Long> b = plantel(equipoB);
            a.retainAll(b);
            return a;
        }

        public Set<Long> diferenciaPlanteles(String equipoA, String equipoB) {
            Set<Long> a = plantel(equipoA);
            Set<Long> b = plantel(equipoB);
            a.removeAll(b);
            return a;
        }

        // ---------- Impresión en tablas ----------
        public void imprimirTablaJugadores(Collection<Long> dnis) {
            String formato = "| %-10s | %-20s | %-3s | %-10s |%n";
            String linea = "+------------+----------------------+-----+------------+";
            System.out.println(linea);
            System.out.printf(formato, "DNI", "Nombre", "Edad", "Posición");
            System.out.println(linea);
            for (long dni : dnis) {
                Jugador j = jugadores.get(dni);
                if (j != null) {
                    System.out.printf(formato, j.getDni(), truncate(j.getNombre(), 20),
                            j.getEdad(), truncate(j.getPosicion(), 10));
                }
            }
            System.out.println(linea);
        }

        public void imprimirTablaJugadoresTodos() {
            imprimirTablaJugadores(jugadores.keySet());
        }

        public void imprimirTablaEquipos() {
            String formato = "| %-20s | %-10s | %-10s |%n";
            String linea = "+----------------------+------------+------------+";
            System.out.println(linea);
            System.out.printf(formato, "Equipo", "Cupo", "Inscritos");
            System.out.println(linea);
            List<String> nombres = new ArrayList<>(equipos.keySet());
            Collections.sort(nombres);
            for (String nombre : nombres) {
                Equipo e = equipos.get(nombre);
                int inscritos = planteles.getOrDefault(nombre, Collections.emptySet()).size();
                System.out.printf(formato, truncate(e.getNombre(), 20),
                        e.getCupoMaximo(), inscritos);
            }
            System.out.println(linea);
        }

        public void imprimirPlantel(String nombreEquipo) {
            Set<Long> set = planteles.get(nombreEquipo);
            if (set == null) {
                System.out.println("Equipo no encontrado.");
                return;
            }
            System.out.println("Plantel de " + nombreEquipo + " (" + set.size() + "):");
            imprimirTablaJugadores(set);
        }

        // ---------- Benchmark ----------
        public void benchmark(int totalJugadores, int totalEquipos, double porcentajeAsignacion, int cupoPorEquipo) {
            System.out.println("== Benchmark ==");
            limpiar();

            long t0 = System.nanoTime();
            // Registrar equipos
            for (int i = 1; i <= totalEquipos; i++) {
                registrarEquipo(new Equipo("Equipo_" + i, cupoPorEquipo));
            }
            long t1 = System.nanoTime();

            // Registrar jugadores
            for (int i = 1; i <= totalJugadores; i++) {
                long dni = i; // único
                Jugador j = new Jugador(dni,
                        "Jugador_" + i,
                        16 + (i % 25),
                        posicionAleatoria(i));
                registrarJugador(j);
            }
            long t2 = System.nanoTime();

            // Asignar ~porcentajeAsignacion respetando cupos
            int objetivoAsignar = (int) Math.round(totalJugadores * porcentajeAsignacion);
            List<Long> dnis = new ArrayList<>(jugadores.keySet());
            Collections.shuffle(dnis, ThreadLocalRandom.current());

            List<String> nombresEquipos = new ArrayList<>(equipos.keySet());
            int asignados = 0;
            for (long dni : dnis) {
                if (asignados >= objetivoAsignar) break;
                // intentar 3 equipos al azar para reducir colisiones de cupo lleno
                boolean puesto = false;
                for (int intentos = 0; intentos < 3 && !puesto; intentos++) {
                    String eq = nombresEquipos.get(ThreadLocalRandom.current().nextInt(nombresEquipos.size()));
                    if (asignarJugadorAEquipo(dni, eq)) {
                        asignados++;
                        puesto = true;
                    }
                }
            }
            long t3 = System.nanoTime();

            // Consultas
            Set<Long> noAsignados = jugadoresNoAsignados();
            List<String> vacios = equiposSinJugadores();
            // Union / intersección de dos equipos arbitrarios
            String e1 = "Equipo_1";
            String e2 = "Equipo_2";
            Set<Long> union = unionPlanteles(e1, e2);
            Set<Long> inter = interseccionPlanteles(e1, e2);

            long t4 = System.nanoTime();

            // Reporte de tiempos
            System.out.printf(Locale.US, "- Registro de %d equipos: %.3f ms%n", totalEquipos, ms(t0, t1));
            System.out.printf(Locale.US, "- Registro de %d jugadores: %.3f ms%n", totalJugadores, ms(t1, t2));
            System.out.printf(Locale.US, "- Asignación de %d jugadores (~%.0f%%): %.3f ms%n", asignados, porcentajeAsignacion*100, ms(t2, t3));
            System.out.printf(Locale.US, "- Consultas (no asignados, vacíos, unión/intersección): %.3f ms%n", ms(t3, t4));

            // Resumen
            System.out.println("\n== Resumen ==");
            System.out.println("Jugadores totales: " + jugadores.size());
            System.out.println("Equipos totales: " + equipos.size());
            System.out.println("Asignados: " + asignados);
            System.out.println("No asignados: " + noAsignados.size());
            System.out.println("Equipos sin jugadores: " + vacios.size());
            System.out.println("Unión " + e1 + " ∪ " + e2 + ": " + union.size());
            System.out.println("Intersección " + e1 + " ∩ " + e2 + ": " + inter.size());

            // Mostrar tablas resumidas
            System.out.println("\n== Tabla de equipos ==");
            imprimirTablaEquipos();
            System.out.println("== Primeros 10 jugadores sin asignar ==");
            imprimirTablaJugadores(noAsignados.stream().limit(10).toList());
        }

        public void limpiar() {
            jugadores.clear();
            equipos.clear();
            planteles.clear();
        }

        // ---------- Utilidades internas ----------
        private static String posicionAleatoria(int seed) {
            String[] pos = {"Arquero", "Defensa", "Volante", "Delantero"};
            return pos[seed % pos.length];
        }

        private static double ms(long tIni, long tFin) {
            return (tFin - tIni) / 1_000_000.0;
        }

        private static String truncate(String s, int n) {
            if (s == null) return "";
            return s.length() <= n ? s : s.substring(0, n - 1) + "…";
        }
    }

    // =====================================
    // ======= INTERFAZ DE CONSOLA UI ======
    // =====================================

    private static void menu() {
        System.out.println("""
                \n===== TORNEO (HashMap/HashSet) =====
                1) Registrar jugador
                2) Registrar equipo
                3) Asignar jugador a equipo
                4) Retirar jugador de equipo
                5) Listar jugadores
                6) Listar equipos
                7) Equipos sin jugadores
                8) Jugadores no asignados
                9) Ver plantel de un equipo
                10) Unión/Intersección/Diferencia de planteles
                11) Benchmark (20k jugadores, 30 equipos, 85% asignación)
                0) Salir
                Seleccione una opción: """);
    }

    public static void main(String[] args) {
        RegistroTorneo rt = new RegistroTorneo();
        Scanner sc = new Scanner(System.in);

        // Datos de muestra mínimos para empezar
        rt.registrarEquipo(new Equipo("Aves Rojas", 25));
        rt.registrarEquipo(new Equipo("Pumas Azules", 25));
        rt.registrarJugador(new Jugador(1001, "Ana Ríos", 21, "Volante"));
        rt.registrarJugador(new Jugador(1002, "Luis Pérez", 24, "Delantero"));

        while (true) {
            menu();
            String op = sc.nextLine().trim();
            try {
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
                        boolean ok = rt.registrarJugador(new Jugador(dni, nom, edad, pos));
                        System.out.println(ok ? "Jugador registrado." : "DNI duplicado, no registrado.");
                    }
                    case "2" -> {
                        System.out.print("Nombre del equipo: ");
                        String nom = sc.nextLine().trim();
                        System.out.print("Cupo máximo: ");
                        int cupo = Integer.parseInt(sc.nextLine().trim());
                        boolean ok = rt.registrarEquipo(new Equipo(nom, cupo));
                        System.out.println(ok ? "Equipo registrado." : "Equipo duplicado, no registrado.");
                    }
                    case "3" -> {
                        System.out.print("DNI del jugador: ");
                        long dni = Long.parseLong(sc.nextLine().trim());
                        System.out.print("Nombre del equipo: ");
                        String nom = sc.nextLine().trim();
                        boolean ok = rt.asignarJugadorAEquipo(dni, nom);
                        System.out.println(ok ? "Asignado." : "No se pudo asignar (datos inválidos o sin cupo).");
                    }
                    case "4" -> {
                        System.out.print("DNI del jugador: ");
                        long dni = Long.parseLong(sc.nextLine().trim());
                        System.out.print("Nombre del equipo: ");
                        String nom = sc.nextLine().trim();
                        boolean ok = rt.retirarJugadorDeEquipo(dni, nom);
                        System.out.println(ok ? "Retirado." : "No se pudo retirar.");
                    }
                    case "5" -> rt.imprimirTablaJugadoresTodos();
                    case "6" -> rt.imprimirTablaEquipos();
                    case "7" -> {
                        List<String> vacios = rt.equiposSinJugadores();
                        System.out.println("Equipos sin jugadores: " + (vacios.isEmpty() ? "(ninguno)" : vacios));
                    }
                    case "8" -> {
                        Set<Long> noAsig = rt.jugadoresNoAsignados();
                        if (noAsig.isEmpty()) {
                            System.out.println("Todos los jugadores están asignados.");
                        } else {
                            rt.imprimirTablaJugadores(noAsig);
                        }
                    }
                    case "9" -> {
                        System.out.print("Nombre del equipo: ");
                        String nom = sc.nextLine().trim();
                        rt.imprimirPlantel(nom);
                    }
                    case "10" -> {
                        System.out.print("Equipo A: ");
                        String a = sc.nextLine().trim();
                        System.out.print("Equipo B: ");
                        String b = sc.nextLine().trim();

                        Set<Long> u = rt.unionPlanteles(a, b);
                        Set<Long> i = rt.interseccionPlanteles(a, b);
                        Set<Long> d = rt.diferenciaPlanteles(a, b);

                        System.out.println("UNIÓN (A ∪ B), total = " + u.size());
                        rt.imprimirTablaJugadores(u.stream().limit(15).toList());

                        System.out.println("INTERSECCIÓN (A ∩ B), total = " + i.size());
                        if (i.isEmpty()) System.out.println("(Vacío, no hay repetidos entre equipos)");
                        else rt.imprimirTablaJugadores(i.stream().limit(15).toList());

                        System.out.println("DIFERENCIA (A − B), total = " + d.size());
                        rt.imprimirTablaJugadores(d.stream().limit(15).toList());
                    }
                    case "11" -> {
                        // Cupo grande para poder alojar ~17k en 30 equipos
                        rt.benchmark(20_000, 30, 0.85, 800);
                    }
                    case "0" -> {
                        System.out.println("¡Hasta luego!");
                        return;
                    }
                    default -> System.out.println("Opción no válida.");
                }
            } catch (Exception ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        }
    }
}
