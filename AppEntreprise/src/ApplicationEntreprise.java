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
    private PreparedStatement voirLesMotsCle;
    private PreparedStatement ajouterMotCle;
    private PreparedStatement voirSesOffres;
    private PreparedStatement voirCandidaturePourOffre;

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
            voirLesMotsCle = connection.prepareStatement("SELECT * FROM projet2023.voir_mots_cles");
            ajouterMotCle = connection.prepareStatement("SELECT projet2023.ajouter_mot_cle_a_offre(?, ?, ?)");
            voirSesOffres = connection.prepareStatement("SELECT * FROM projet2023.voir_ses_offres_de_stage WHERE entreprise = ?");
            voirCandidaturePourOffre = connection.prepareStatement("SELECT * FROM projet2023.voir_candidature_pour_offre_de_stage WHERE code = ? AND entreprise = ?");

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
            System.out.println("Erreur lors de la connexion de l'entreprise");
        }
    }

    private void menuEntreprise() throws SQLException {
        int choix;
        while (true) {
            System.out.println("============================ Application Entreprise ============================");
            System.out.println("1. Encoder une offre de stage");
            System.out.println("2. Voir les mots clés");
            System.out.println("3. Ajouter un mot clé à une offre de stage");
            System.out.println("4. Voir ses offres de stage");
            System.out.println("5. Voir les candidatures pour une offre de stage");
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
                case 2 -> voirLesMotsCles();
                case 3 -> ajouterMotCle();
                case 4 -> voirSesOffres();
                case 5 -> voirCandidaturePourOffre();
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
            System.out.println("Erreur lors de la fermeture de la connexion à la base de données");
            // System.out.println(e.getMessage());
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

    private void voirLesMotsCles() throws SQLException {
        System.out.println("================================ Voir les mots clés =================================");
        ResultSet rs = null;

        try {
            voirLesMotsCle.execute();
            rs = voirLesMotsCle.getResultSet();
            while (rs.next()) {
                System.out.println(rs.getInt("id_mot_cle") + " : " +  rs.getString("mots_cles"));
            }
            System.out.println("Mots clés affichés avec succès");
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'affichage des mots clés");
            // System.out.println(e.getMessage());
        }

        System.out.println("====================================================================================\n");
    }

    private void ajouterMotCle() {
        System.out.println("================================ Ajouter un mot clé à une offre de stage =================================");
        System.out.print("Entrez l'id de l'offre de stage: ");
        String idOffre = scanner.nextLine();
        System.out.print("Entrez le mot clé: ");
        String motCle = scanner.nextLine();

        try {
            ajouterMotCle.setString(1,idEntreprise);
            ajouterMotCle.setString(2,idOffre);
            ajouterMotCle.setString(3,motCle);

            ajouterMotCle.execute();
            System.out.println("Mot clé ajouté avec succès");
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'ajout du mot clé");
            // System.out.println(e.getMessage());
        }

        System.out.println("============================================================================================================\n");
    }

    private void voirSesOffres() {
        System.out.println("================================ Voir ses offres de stage =================================");
        ResultSet rs = null;

        try {
            voirSesOffres.setString(1, idEntreprise);
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'affichage des offres de stage");
            // System.out.println(e.getMessage());
        }

        try {
            voirSesOffres.execute();

            rs = voirSesOffres.getResultSet();
            while (rs.next()) {
                System.out.println("Offre : " + rs.getString("code"));
                System.out.println("↳ " + rs.getString("description"));
                System.out.println("↳ " + rs.getString("semestre"));
                System.out.println("↳ " + rs.getString("etat"));
                System.out.println("↳ " + rs.getString("nombre_candidature"));
                System.out.println("↳ " + rs.getString("etudiant"));
                System.out.println();
            }
            System.out.println("Offres affichées avec succès");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        System.out.println("============================================================================================\n");
    }

    private void voirCandidaturePourOffre() {
        System.out.println("================================ Voir les candidatures pour une offre de stage =================================");
        System.out.print("Entrez le code de l'offre de stage: ");
        String codeOffre = scanner.nextLine();

        int numCandidature = 1;
        ResultSet rs = null;

        try {
            voirCandidaturePourOffre.setString(1, codeOffre);
            voirCandidaturePourOffre.setString(2, idEntreprise);
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'affichage des candidatures pour une offre de stage");
            // System.out.println(e.getMessage());
        }

        try {
            voirCandidaturePourOffre.execute();

            rs = voirCandidaturePourOffre.getResultSet();
            if (!rs.isBeforeFirst()) {
                System.out.println("Aucune candidature pour cette offre de stage");
                return;
            }

            while (rs.next()) {
                System.out.println("Candidature : " + numCandidature++);
                System.out.println("↳ État : " + rs.getString("etat"));
                System.out.println("↳ Nom : " + rs.getString("nom"));
                System.out.println("↳ Prénom : " + rs.getString("prenom"));
                System.out.println("↳ Email : " + rs.getString("email"));
                System.out.println("↳ Motivation : " + rs.getString("motivation"));
                System.out.println();
            }
            System.out.println("Candidatures affichées avec succès");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

}
