import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.InputMismatchException;
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
            connecterEtudiant =
                    connection.prepareStatement("SELECT projet2023.connecter_etudiant(?)");
            soumettreCandidature =
                    connection.prepareStatement("SELECT projet2023.soumettre_candidature(?,?,?) ");
            annulerCandidatures =
                    connection.prepareStatement("SELECT projet2023.annuler_candidature(?,?)");
            voirSesCandidaturess =
                    connection.prepareStatement("SELECT * FROM projet2023.voir_les_candidatures_d_un_etudiant WHERE etudiant = ? ");
            voirOffreDeStageValidee =
                    connection.prepareStatement("SELECT * FROM projet2023.offre_stage_validee WHERE etudiant = ?");
            rechercheOffreDeStageParMotCle =
                    connection.prepareStatement("SELECT *  FROM projet2023.recherche_par_mot_clees WHERE etudiant = ? AND mot_cle = ?");


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

    public void menuEtudiant() {
        int choix = 0;
        while (true) {
            System.out.println("============================ Application Etudiant ============================");
            System.out.println("1. Voir les offres de stage validée");
            System.out.println("2. Rechercher une offre de stage par mots cles");
            System.out.println("3. Poser sa candidature");
            System.out.println("4. Voir les offres de stage pour lesquelles une candidature a été posée");
            System.out.println("5. Annuler une candidature");
            System.out.println("6. Fermer l'application");
            System.out.println("==============================================================================");
            System.out.print("Entrez votre choix: ");

            try {
                choix = scanner.nextInt();
                scanner.nextLine(); // Pour consommer la nouvelle ligne restante
                System.out.println();

                if (choix == 0) break;

                if (choix < 0 || choix > 6) {
                    System.out.println("Erreur: Veuillez entrer un nombre entre 1 et 6");
                    continue;
                }

                switch (choix) {
                    case 1: {
                        voirOffreDeStageValidee();
                        break;
                    }
                    case 2: {
                        rechercheOffreDeStageParMotCle();
                        break;
                    }
                    case 3: {
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
                        break;
                    }
                }

            } catch (InputMismatchException e) {
                System.out.println("Erreur: Veuillez entrer un nombre");
                scanner.nextLine(); // Pour consommer la mauvaise entrée

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

    private void voirOffreDeStageValidee() {
        System.out.println("============================== Voir les offres de stages validées =============================\n");
        ResultSet rs = null;

        try {
            voirOffreDeStageValidee.setInt(1, Integer.parseInt(idEtudiant));
        } catch (SQLException e) {
            System.out.println("ERROR: Une erreur est survenue");
        }

        try {
            voirOffreDeStageValidee.execute();

            rs = voirOffreDeStageValidee.getResultSet();
            while(rs.next()) {
                System.out.println("Offre : " + rs.getString("code"));
                System.out.println("↳ " + rs.getString("nom"));
                System.out.println("↳ " + rs.getString("description"));
                System.out.println("↳ " + rs.getString("semestre"));
                System.out.println("↳ " + rs.getString("mot_cle"));
                System.out.println();
            }
            System.out.println("Offres de stage affichées avec succès");
        } catch (SQLException e) {
            System.out.println("ERROR: Une erreur est survenue");
            System.out.println(e.getMessage());
        }

        System.out.println("====================================================================================\n");
    }

    private void rechercheOffreDeStageParMotCle() {
        System.out.println("================================ Recherche par mots cles ===========================\n");
        System.out.println("Entrez un mot-cle : ");
        String motCle = scanner.nextLine().toLowerCase();

        ResultSet rs = null;

        try {
            rechercheOffreDeStageParMotCle.setInt(1, Integer.parseInt(idEtudiant));
            rechercheOffreDeStageParMotCle.setString(2, motCle);
        } catch (SQLException e) {
            System.out.println("ERROR : une erreur est survenue");
        }

        try {
            rechercheOffreDeStageParMotCle.execute();

            rs = rechercheOffreDeStageParMotCle.getResultSet();

            if (!rs.isBeforeFirst()) {
                System.out.println("Aucune candidature trouvée pour le mot clé : " + motCle + "\n");
                return;
            }

            while(rs.next()) {
                System.out.println("Offre : " + rs.getString("code"));
                System.out.println("↳ " + rs.getString("nom"));
                System.out.println("↳ " + rs.getString("description"));
                System.out.println("↳ " + rs.getString("semestre"));
                System.out.println("↳ " + rs.getString("mot_cle"));
                System.out.println();
            }
            System.out.println("Offres de stage affichées avec succès");

        } catch (SQLException e) {
            System.out.println("ERROR : une erreur est survenue");
        }
    }

    private void poserSaCandidature() {

        System.out.println("================================ soumettre une candidature =================================");
        System.out.println("Entrez votre motivation : ");
        String motivation = scanner.nextLine();
        System.out.print("le code de l'offre de stage : ");
        String codeOffreStage = scanner.nextLine();


        try {
            soumettreCandidature.setString(1, motivation);
            soumettreCandidature.setInt(2, Integer.parseInt(idEtudiant));
            soumettreCandidature.setString(3, codeOffreStage);

            soumettreCandidature.execute();
            System.out.println("Offre de stage encodée avec succès");
        } catch (SQLException e) {
            System.out.println("ERROR : une erreur est survenue");

        }
        System.out.println("==============================================================================================\n");
    }




    private void voirOffreDeStageAvecCandidaturePosee(){
        System.out.println("================================ Voir les offres de stage candidatées =================================");
        ResultSet rs = null;

        try {
            voirSesCandidaturess.setInt(1, Integer.parseInt(idEtudiant));
        } catch (SQLException e) {
            System.out.println("ERROR : une erreur est survenue lors de la recuperation des candidatures");
        }

        try {
            voirSesCandidaturess.execute();

            rs = voirSesCandidaturess.getResultSet();

            if (!rs.isBeforeFirst()) {
                System.out.println("Vous n'avez posé aucune candidature\n");
                return;
            }

            while(rs.next()) {
                System.out.println("Offre : " + rs.getString("code"));
                System.out.println("↳ Entreprise : " + rs.getString("nom_entreprise"));
                System.out.println("↳ Etat : " + rs.getString("etat_candidature"));
                System.out.println();
            }
            System.out.println("Candidatures récupérées avec succès");
        } catch (SQLException e) {
            System.out.println("ERROR : une erreur est survenue lors de la recuperation des candidatures");
        }
        System.out.println("========================================================================================================\n");
    }

    private void annulerUneCandidature(){
        System.out.println("================================ Annuler une candidature =================================");
        System.out.println("Entrez le code de l'offre de stage : ");
        String codeOffre = scanner.nextLine();

        ResultSet rs = null;

        try {
            annulerCandidatures.setString(1, codeOffre);
            annulerCandidatures.setInt(2, Integer.parseInt(idEtudiant));
        } catch (SQLException e) {
            System.out.println("ERROR : une erreur est survenue lors de la recuperation des candidatures");
        }

        try {
            annulerCandidatures.execute();

            System.out.println("Candidature annulée avec succès");
        } catch (SQLException e) {
            System.out.println("ERROR : une erreur est survenue lors de la recuperation des candidatures");
        }
        System.out.println("========================================================================================================\n");
    }
}