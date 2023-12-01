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

    private PreparedStatement connecterEtudiant;

    private PreparedStatement soumettreCandidature;

    private PreparedStatement annulerCandidatures;

    private PreparedStatement voirSesCandidaturess;

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
        String user = properties.getProperty("db.user");
        String password = properties.getProperty("db.password");

        try {
            // Connect to the database
            connection = DriverManager.getConnection(url, user, password);

            // Prepare the SQL statement
            connecterEtudiant = connection.prepareStatement("SELECT projet2023.connecter_etudiant(?)");
            soumettreCandidature = connection.prepareStatement("SELECT projet2023.soumettre_candidature(?,?,?,?) ");
            annulerCandidatures = connection.prepareStatement("SELECT projet2023.annuler_candidature(?,?)");
            voirSesCandidaturess = connection.prepareStatement("SELECT * FROM projet2023.voir_les_candidatures_d_un_etudiant WHERE etudiant = ? ");
            voirOffreDeStageValidee = connection.prepareStatement("SELECT * FROM projet2023.offre_stage_validee WHERE etudiant = ?");
            rechercheOffreDeStageParMotCle = connection.prepareStatement("SELECT *  FROM projet2023.recherche_par_mot_clees WHERE etudiant = ? AND mot_cle = ?");


        } catch (SQLException e) {
            System.out.println("Erreur de la connexion à la base de données : " + e.getMessage());
            System.exit(1);
        }

    }

    public void start() {
        int option = 0;

        while (true) {
            System.out.println("==================== Application Etudiant ====================");
            System.out.println("1. Se connecter");
            System.out.println("2. Fermer l'application");
            System.out.println("==============================================");
            System.out.print("Entrez votre choix: ");

            try {
                option = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Erreur de saisie");
                continue;
            }

            if (option == 0) break;

            if (option < 0 || option > 2) {
                System.out.println("Erreur: Veuillez entrer un nombre entre 1 et 2");
                continue;
            }

            switch (option) {
                case 1: {
                    seConnecter();
                    break;
                }
                case 2: {
                    System.out.println("Fermeture de l'application");
                    this.close();
                    System.exit(0);
                    break;
                }
            }
        }
    }

    public void seConnecter(){
        System.out.println("================================ Connexion Etudiant =================================");
        System.out.print("Entrez votre email: ");
        String email = scanner.next();
        System.out.print("Entrez votre mot de passe: ");
        String password = scanner.next();

        try {
            connecterEtudiant.setString(1,email);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println("Erreur lors de la connexion de l'etudiant");
        }
        try (ResultSet resultSet = connecterEtudiant.executeQuery()) {
            if (resultSet.next()) {
                if (BCrypt.checkpw(password, resultSet.getString("connecter_etudiant").split(",")[1].replace(")",""))) {
                    idEtudiant = resultSet.getString("connecter_etudiant").split(",")[0].replace("(","");
                    System.out.println("Connecter avec succès sous l'identifiant : " + idEtudiant + "\n");
                    menuEtudiant();
                } else {
                    System.out.println("mots de passe ou e-mail invalide");
                    seConnecter();
                }
            } else {
                System.out.println("mots de passe ou e-mail invalide");
                seConnecter();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println("Erreur lors de la connexion de l'etudiant");
        }
    }
    public void menuEtudiant(){
        int choix;
        while (true) {
            System.out.println("============================ Application Etudiant ============================");
            System.out.println("1. Voir les offres de stage validee");
            System.out.println("2. Rechercher une offre de stage par mots cles");
            System.out.println("3. Poser sa candidature");
            System.out.println("4. Voir les offres de stage pour lequel une candidature a ete posee");
            System.out.println("5. Annuler une candidature");
            System.out.println("6. fermer l'application");
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

            if (choix < 0 || choix > 6) {
                System.out.println("Erreur: Veuillez entrer un nombre entre 1 et 5");
                continue;
            }

            switch (choix) {
                case 1: {
                    voirOffreDeStageValidee();
                    break;
                }
                case 2:{
                    rechercheOffreDeStageParMotCle();
                    break;
                }
                case 3:{
                    poserSaCandidature();
                    break;
                }
                case 4: {
                    voirOffreDeStageAvecCandidaturePosee();
                    break;
                }
                case 5: {
                    annulerUneCandidature();
                    break;
                }
                case 6: {
                    System.out.println("Fermeture de l'application");
                    this.close();
                    System.exit(0);
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

    private void voirOffreDeStageValidee(){

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

    private void rechercheOffreDeStageParMotCle(){

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

    private void poserSaCandidature(){

    }

    private void voirOffreDeStageAvecCandidaturePosee(){

    }

    private void annulerUneCandidature(){

    }
}