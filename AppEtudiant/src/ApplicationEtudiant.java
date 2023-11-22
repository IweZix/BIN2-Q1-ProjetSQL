import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Scanner;
import java.sql.*;

public class ApplicationEtudiant {

    Scanner scanner = new Scanner(System.in);

    private static final String CONFIG_FILE = "./config.properties";

    private Connection connection;

    private PreparedStatement voirOffreDeStageValidee;

    private PreparedStatement rechercheOffreDeStageParMotCle;

    private String idEtudiant;

    public ApplicationEtudiant() {

        // Load the PostgreSQL JDBC driver
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Driver PostgreSQL manquant !");
            System.exit(1);
        }

        Properties properties = loadProperties();

        // URL to connect to the database local
        String url = properties.getProperty("db.url");

        try {
            // Connect to the database
            connection = DriverManager.getConnection(url, properties.getProperty("db.user"), properties.getProperty("db.password"));

            // Prepare the SQL statement
            voirOffreDeStageValidee = connection.prepareStatement("SELECT SELECT code, nom, adresse, description FROM projet2023.voir_offres_de_stage_validee WHERE etudiant = ?");
            rechercheOffreDeStageParMotCle = connection.prepareStatement("SELECT code, nom, adresse, description, mots_cles FROM projet2023.recherche_offre_de_stage_par_mots_cle(?,?) WHERE etudiant = $1 AND mots_cles = $2");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }

    }

    public void start() throws SQLException {
        int choix;
        while (true) {
            System.out.println("============================ Application Etudiant ============================");
            System.out.println("1. Voir les offres de stage validee");
            System.out.println("2. Rechercher une offre de stage par mots cles");
            System.out.println("3. Poser sa candidature");
            System.out.println("4. Voir les offres de stage pour lequel une candidature a ete posee");
            System.out.println("5. Annuler une candidature");
            System.out.println("==============================================================================");
            System.out.print("Entrez votre choix: ");

            try {
                choix = Integer.parseInt(scanner.nextLine());
                System.out.println();
            } catch (NumberFormatException e) {
                System.out.println("Erreur: Veuillez entrer un nombre");
                continue;
            }

            if (choix == 0) break;

            if (choix < 0 || choix > 5) {
                System.out.println("Erreur: Veuillez entrer un nombre entre 1 et 5");
                continue;
            }

            switch (choix) {
                case 1 -> voirOffreDeStageValidee();
                case 2 -> rechercheOffreDeStageParMotCle();
                case 3 -> poserSaCandidature();
                case 4 -> voirOffreDeStageAvecCandidaturePosee();
                case 5 -> annulerUneCandidature();
                case 6 -> {
                    System.out.println("Fermeture de l'application");
                    this.close();
                    System.exit(0);
                }
                default -> {
                }
            }
        }

    }

    /**
     * Load the configuration file for the database
     *
     * @return the properties loaded
     */
    private Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream input = new FileInputStream(CONFIG_FILE)) {
            properties.load(input);
        } catch (IOException e) {
            System.out.println("Erreur lors du chargement du fichier de configuration : " + e.getMessage());
            System.exit(1);
        }
        return properties;
    }

    private void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            System.out.println("Erreur lors de la fermeture de la connexion à la base de données");
            // System.out.println(e.getMessage());
        }
    }

    private void voirOffreDeStageValidee() throws SQLException {

        try {
            System.out.println("====================================================================================\n");
            System.out.println("voici les offres de stages");
            voirOffreDeStageValidee.execute();

        } catch (SQLException e) {
            System.out.println("ERROR: Une erreur est survenue");
            // System.out.println(e.getMessage());
        }

        System.out.println("====================================================================================\n");
    }

    private void rechercheOffreDeStageParMotCle() throws SQLException {

        try {
            System.out.println("====================================================================================\n");
            System.out.println("Entrez un mot-cle : ");
            String motscle = scanner.nextLine();
            rechercheOffreDeStageParMotCle.setString(1, idEtudiant);
            rechercheOffreDeStageParMotCle.setString(2, motscle);
            System.out.println("voici les offres de stages par mots-cles");
            rechercheOffreDeStageParMotCle.execute();


        } catch (SQLException e) {
            System.out.println("ERROR : une erreur est survenue");
            // System.out.println(e.getMessage());
        }
    }

    private void poserSaCandidature() throws SQLException {

    }

    private void voirOffreDeStageAvecCandidaturePosee() throws SQLException {

    }

    private void annulerUneCandidature() throws SQLException {

    }
}