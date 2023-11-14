import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Scanner;
import java.io.InputStream;
import java.util.Properties;

public class ApplicationProfesseur {

    private final Scanner scanner = new Scanner(System.in);

    // Load the configuration file for the database
    private static final String CONFIG_FILE = "./config.properties";

    private Connection connection;

    private PreparedStatement encoderEtudiant;
    private PreparedStatement encoderEntreprise;
    private PreparedStatement encoderMotCle;
    private PreparedStatement voirOffreDeStageNonValidee;
    private PreparedStatement validerOffreDeStageNonValidee;
    private PreparedStatement voirOffreDeStageValidee;
    private PreparedStatement voirEtudiantSansStageAccepte;
    private PreparedStatement voirOffreDeStageAttribuee;

    public ApplicationProfesseur() {

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

        // Try to connect
        try {
            // Connect to the database
            connection = DriverManager.getConnection(url, properties.getProperty("db.user"), properties.getProperty("db.password"));

            // Prepare the SQL statement
            encoderEtudiant = connection.prepareStatement("SELECT projet2023.encoder_etudiant(?, ?, ?, ?, ?)");
            encoderEntreprise = connection.prepareStatement("SELECT projet2023.encoder_entreprise(?, ?, ?, ?)");
            encoderMotCle = connection.prepareStatement("SELECT projet2023.encoder_un_mot_cle(?)");
            voirOffreDeStageNonValidee = connection.prepareStatement("SELECT * FROM projet2023.voir_offres_de_stage_non_validee");
            validerOffreDeStageNonValidee = connection.prepareStatement("SELECT FROM projet2023.valider_offre_de_stage(?)");
            voirOffreDeStageValidee = connection.prepareStatement("SELECT * FROM projet2023.voir_offres_de_stage_validee");
            voirEtudiantSansStageAccepte = connection.prepareStatement("SELECT * FROM projet2023.voir_etudiant_sans_stage_accepte");
            voirOffreDeStageAttribuee = connection.prepareStatement("SELECT * FROM projet2023.voir_offres_de_stage_aatribuee");


        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }

    public void start() throws SQLException {
        int choix;
        while (true) {
            System.out.println("============================ Application Centrale ============================");
            System.out.println("1. Encoder un étudiant");
            System.out.println("2. Encoder une entreprise");
            System.out.println("3. Encoder un mot clé");
            System.out.println("4. Voir les offres de stage non validées");
            System.out.println("5. Valider une offre de stage non validée");
            System.out.println("6. Voir les offres de stage validées");
            System.out.println("7. Voir les étudiants sans stage accepté");
            System.out.println("8. Voir les offres de stage attribuées");
            System.out.println("9. Fermer l'application");
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

            if (choix < 0 || choix > 9) {
                System.out.println("Erreur: Veuillez entrer un nombre entre 1 et 9");
                continue;
            }

            switch (choix) {
                case 1 -> encoderEtudiant();
                case 2 -> encoderEntreprise();
                case 3 -> encoderMotCle();
                case 4 -> voirOffreDeStageNonValidee();
                case 5 -> validerOffreDeStageNonValidee();
                case 6 -> voirOffreDeStageValidee();
                case 7 -> voirEtudiantSansStageAccepte();
                case 8 -> voirOffreDeStageAttribuee();
                case 9 -> {
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

    private void encoderEtudiant() throws SQLException {
        System.out.println("================================ Encoder un étudiant ================================");
        System.out.print("Entrez le nom de l'étudiant: ");
        String nom = scanner.nextLine();
        System.out.print("Entrez le prénom de l'étudiant: ");
        String prenom = scanner.nextLine();
        System.out.print("Entre l'email de l'étudiant: ");
        String email = scanner.nextLine();
        System.out.print("Entrez le semestre de l'étudiant: ");
        String semestre = scanner.nextLine();
        System.out.print("Entrez le mot de passe de l'étudiant: ");
        String motDePasse = scanner.nextLine();

        try {
            encoderEtudiant.setString(1, nom);
            encoderEtudiant.setString(2, prenom);
            encoderEtudiant.setString(3, email);
            encoderEtudiant.setString(4, semestre);
            encoderEtudiant.setString(5, motDePasse);

            encoderEtudiant.execute();
            System.out.println("Etudiant encodé avec succès");
        } catch (SQLException e) {
            System.out.println("ERROR: Une erreur est survenue");
            // System.out.println(e.getMessage());
        }

        System.out.println("====================================================================================\n");
    }

    private void encoderEntreprise() throws SQLException {
        System.out.println("================================ Encoder une entreprise ================================");
        System.out.print("Entrez le nom de l'entreprise: ");
        String nom = scanner.nextLine();
        System.out.print("Entrez l'adresse de l'entreprise: ");
        String adresse = scanner.nextLine();
        System.out.print("Entrez l'email de l'entreprise: ");
        String email = scanner.nextLine();
        System.out.print("Entrez le mot de passe de l'entreprise: ");
        String motDePasse = scanner.nextLine();

        try {
            encoderEntreprise.setString(1, nom);
            encoderEntreprise.setString(2, adresse);
            encoderEntreprise.setString(3, email);
            encoderEntreprise.setString(4, motDePasse);

            encoderEntreprise.execute();
            System.out.println("Entreprise encodée avec succès");
        } catch (SQLException e) {
            System.out.println("ERROR: Une erreur est survenue");
            // System.out.println(e.getMessage());
        }
        System.out.println("====================================================================================\n");

    }

    private void encoderMotCle() throws SQLException {
        System.out.println("================================ Encoder un mot clé ================================");
        System.out.print("Entrez le mot clé: ");
        String motCle = scanner.nextLine();

        try {
            encoderMotCle.setString(1, motCle);

            encoderMotCle.execute();
            System.out.println("Mot clé encodé avec succès");
        } catch (SQLException e) {
            System.out.println("ERROR: Une erreur est survenue");
            // System.out.println(e.getMessage());
        }

        System.out.println("====================================================================================\n");
    }

    private void voirOffreDeStageNonValidee() throws SQLException {
        System.out.println("================================ Voir les offres de stage non validées ================================");
        ResultSet rs = null;

        try {
            voirOffreDeStageNonValidee.execute();
            rs = voirOffreDeStageNonValidee.getResultSet();
        } catch (SQLException e) {
            System.out.println("ERROR: Une erreur est survenue");
            // System.out.println(e.getMessage());
        }

        // si aucune offre de stage non validée
        if (!rs.isBeforeFirst()) {
            System.out.println("Aucune offre de stage non validée n'a été trouvée.");
        } else {
            // affichage des offres de stage non validées
            while (rs.next()) {
                System.out.println("Offre de stage non validée " + rs.getString("code"));
                System.out.println("↳ Code: " + rs.getString("code"));
                System.out.println("↳ Semestre: " + rs.getString("semestre"));
                System.out.println("↳ Nom: " + rs.getString("nom"));
                System.out.println("↳ Description: " + rs.getString("description") + "\n");
            }
            System.out.println("Offres de stage non validées affichées avec succès");
        }
        System.out.println("=======================================================================================================\n");
    }

    private void validerOffreDeStageNonValidee() throws SQLException {
        System.out.println("================================ Valider une offre de stage non validée ================================");
        System.out.println("Entrez le code de l'offre de stage à valider: ");
        String code = scanner.nextLine();

        try {
            validerOffreDeStageNonValidee.setString(1, code);
            validerOffreDeStageNonValidee.execute();
            System.out.println("Offre de stage validée avec succès");
        } catch (SQLException e) {
            String message = e.getMessage();
            int newLineIndex = message.indexOf("\n");
            System.out.println(message.substring(0, newLineIndex));
        }

        System.out.println("=======================================================================================================\n");
    }

    private void voirOffreDeStageValidee() throws SQLException {
        System.out.println("================================ Voir les offres de stage validées ================================");

        ResultSet rs = null;

        try {
            voirOffreDeStageValidee.execute();
            rs = voirOffreDeStageValidee.getResultSet();
        } catch (SQLException e) {
            System.out.println("ERROR: Une erreur est survenue");
            // System.out.println(e.getMessage());
        }

        if (!rs.isBeforeFirst()) {
            System.out.println("Aucune offre de stage validée n'a été trouvée.");
        } else {
            // affichage des offres de stage validées
            while (rs.next()) {
                System.out.println("Offre de stage validée " + rs.getString("code"));
                System.out.println("↳ Code: " + rs.getString("code"));
                System.out.println("↳ Semestre: " + rs.getString("semestre"));
                System.out.println("↳ Nom: " + rs.getString("nom"));
                System.out.println("↳ Description: " + rs.getString("description") + "\n");
            }

            System.out.println("Offres de stage validées affichées avec succès");
        }
        System.out.println("=======================================================================================================\n");
    }

    private void voirEtudiantSansStageAccepte() throws SQLException {
        System.out.println("================================ Voir les étudiants sans stage accepté ================================");

        ResultSet rs = null;

        try {
            voirEtudiantSansStageAccepte.execute();
            rs = voirEtudiantSansStageAccepte.getResultSet();
        } catch (SQLException e) {
            System.out.println("ERROR: Une erreur est survenue");
            // System.out.println(e.getMessage());
        }

        if (!rs.isBeforeFirst()) {
            System.out.println("Aucun étudiant sans stage accepté n'a été trouvé.");
        } else {
            // affichage des étudiants sans stage accepté
            while (rs.next()) {
                System.out.println("Etudiant sans stage accepté");
                System.out.println("↳ Nom: " + rs.getString("nom"));
                System.out.println("↳ Prénom: " + rs.getString("prenom"));
                System.out.println("↳ Email: " + rs.getString("email"));
                System.out.println("↳ Semestre: " + rs.getString("semestre"));
                System.out.println("↳ Nombre de candidatures: " + rs.getString("nombre_candidature") + "\n");

            }

            System.out.println("Etudiants sans stage accepté affichés avec succès");
        }
        System.out.println("=======================================================================================================\n");
    }

    private void voirOffreDeStageAttribuee() throws SQLException {
        System.out.println("================================ Voir les offres de stage attribuées ================================");

        ResultSet rs = null;

        try {
            voirOffreDeStageAttribuee.execute();
            rs = voirOffreDeStageAttribuee.getResultSet();
        } catch (SQLException e) {
            System.out.println("ERROR: Une erreur est survenue");
            // System.out.println(e.getMessage());
        }

        if (!rs.isBeforeFirst()) {
            System.out.println("Aucune offre de stage attribuée n'a été trouvée.");
        } else {
            // affichage des offres de stage attribuées
            while (rs.next()) {
                System.out.println("Offre de stage attribuée " + rs.getString("code"));
                System.out.println("↳ Code: " + rs.getString("code"));
                System.out.println("↳ Nom entreprise: " + rs.getString("nom_entreprise"));
                System.out.println("↳ Nom: " + rs.getString("nom"));
                System.out.println("↳ Prénom: " + rs.getString("prenom") + "\n");
            }

            System.out.println("Offres de stage attribuées affichées avec succès");
        }
        System.out.println("=======================================================================================================\n");
    }

}
