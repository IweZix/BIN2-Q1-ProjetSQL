import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Scanner;

public class ApplicationEntreprise {

    private final Scanner scanner = new Scanner(System.in);

    private static final String CONFIG_FILE = "./config.properties";

    private Connection connection;

    private PreparedStatement connecterEntreprise;
    private PreparedStatement encoderOffreDeStage;

    private String idEntreprise;

    public ApplicationEntreprise() {

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Impossible de charger le driver postgresql");
            System.exit(1);
        }

        Properties properties = loadProperties();

        String url = properties.getProperty("db.url");
        String user = properties.getProperty("db.user");
        String password = properties.getProperty("db.password");

        try {
            connection = java.sql.DriverManager.getConnection(url, user, password);

            connecterEntreprise = connection.prepareStatement("SELECT projet2023.connecter_entreprise(?)");
            encoderOffreDeStage = connection.prepareStatement("SELECT projet2023.encoder_offre_de_stage(?, ?, ?)");

        } catch (java.sql.SQLException e) {
            System.err.println("Erreur lors de la connexion à la base de données : " + e.getMessage());
            System.exit(1);
        }
    }

    public void start() {
        int option = 0;

        while (true) {
            System.out.println("==================== Application Entreprise ====================");
            System.out.println("1. Se connecter");
            System.out.println("2. Fermer l'application");
            System.out.println("==============================================");
            System.out.print("Entrez votre choix: ");

            try {
                option = scanner.nextInt();
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

    private void seConnecter() {
        System.out.println("================================ Connexion  Entreprise =================================");
        System.out.print("Entrez votre email: ");
        String email = scanner.next();
        System.out.print("Entrez votre mot de passe: ");
        String password = scanner.next();

        try {
            connecterEntreprise.setString(1,email);
        } catch (SQLException e) {
            System.out.println("Erreur lors de la connexion de l'entreprise");
        }

        try (ResultSet resultSet = connecterEntreprise.executeQuery()) {
            if (resultSet.next()) {
                if (BCrypt.checkpw(password, resultSet.getString("connecter_entreprise").split(",")[1].replace(")",""))) {
                    idEntreprise = resultSet.getString("connecter_entreprise").split(",")[0].replace("(","");
                    System.out.println("Connecter avec succès sous l'identifiant : " + idEntreprise + "\n");
                    menuEntreprise();
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
        }
    }

    private void menuEntreprise() {
        int choix;
        while (true) {
            System.out.println("============================ Application Entreprise ============================");
            System.out.println("1. Encoder une offre de stage");
            System.out.println("7. Fermer l'application");
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

            if (choix < 0 || choix > 7) {
                System.out.println("Erreur: Veuillez entrer un nombre entre 1 et 7");
                continue;
            }

            switch (choix) {
                case 1 -> encoderOffreDeStage();
                case 7 -> {
                    System.out.println("Fermeture de l'application");
                    this.close();
                    System.exit(0);
                }
                default -> {}
            }
        }
    }

    /**
     * Load the configuration file for the database
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
            System.out.println(e.getMessage());
        }
    }

    private void encoderOffreDeStage() {
        System.out.println("================================ Encoder une offre de stage =================================");
        System.out.print("Entrez la description de l'offre de stage: ");
        String description = scanner.nextLine();
        System.out.print("Entrez le semestre de l'offre de stage: ");
        String semestre = scanner.nextLine();

        try {
            encoderOffreDeStage.setString(1,description);
            encoderOffreDeStage.setString(2,semestre);
            encoderOffreDeStage.setString(3,idEntreprise);

            encoderOffreDeStage.execute();
            System.out.println("Offre de stage encodée avec succès");
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'encodage de l'offre de stage");
            // System.out.println(e.getMessage());
        }

        System.out.println("==============================================================================================\n");

    }

}