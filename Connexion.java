package tp_bdd;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class Connexion {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/tp_bdd";
        String user = "root";
        String password = "root";
        Scanner scanner = new Scanner(System.in);

        try (Connection con = DriverManager.getConnection(url, user, password)) {
            if (con != null) {
                System.out.println("Connexion réussie !");

                // Create tables automatically
                createInitialTables(con);

                while (true) {
                    System.out.println("\n--- Menu Principal ---");
                    System.out.println("1. Insérer des données dans une table");
                    System.out.println("2. Afficher les données d'une table");
                    System.out.println("3. Supprimer des données d'une table");
                    System.out.println("4. Quitter");
                    System.out.print("Choisissez une option: ");
                    int choice = scanner.nextInt();
                    scanner.nextLine(); // Consommer la nouvelle ligne

                    switch (choice) {
                        case 1:
                            insertData(con, scanner);
                            break;
                        case 2:
                            displayTableData(con, scanner);
                            break;
                        case 3:
                            deleteData(con, scanner);
                            break;
                        case 4:
                            System.out.println("Au revoir !");
                            scanner.close();
                            return;
                        default:
                            System.out.println("Choix invalide. Veuillez réessayer.");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Fonction pour créer les tables initiales
    public static void createInitialTables(Connection con) throws SQLException {
        try (Statement stmt = con.createStatement()) {
            String[] createTableStatements = {
                "CREATE TABLE IF NOT EXISTS Departement (" +
                "code_departement VARCHAR(10) PRIMARY KEY, " +
                "nom_departement VARCHAR(100) NOT NULL)",
                
                "CREATE TABLE IF NOT EXISTS Employe (" +
                "matricule_employe VARCHAR(10) PRIMARY KEY, " +
                "nom_complet VARCHAR(100) NOT NULL, " +
                "code_departement VARCHAR(10), " +
                "FOREIGN KEY (code_departement) REFERENCES Departement(code_departement))",
                
                "CREATE TABLE IF NOT EXISTS Projet (" +
                "code_projet VARCHAR(10) PRIMARY KEY, " +
                "nom_projet VARCHAR(100) NOT NULL, " +
                "description TEXT, " +
                "date_debut DATE, " +
                "code_departement VARCHAR(10), " +
                "FOREIGN KEY (code_departement) REFERENCES Departement(code_departement))",
                
                "CREATE TABLE IF NOT EXISTS AffectationProjet (" +
                "code_projet VARCHAR(10), " +
                "matricule_employe VARCHAR(10), " +
                "PRIMARY KEY (code_projet, matricule_employe), " +
                "FOREIGN KEY (code_projet) REFERENCES Projet(code_projet), " +
                "FOREIGN KEY (matricule_employe) REFERENCES Employe(matricule_employe))"
            };

            for (String sql : createTableStatements) {
                stmt.executeUpdate(sql);
            }
            System.out.println("Tables initiales créées avec succès !");
        }
    }

    // Fonction pour insérer des données dans une table
    public static void insertData(Connection con, Scanner scanner) throws SQLException {
        System.out.println("Tables disponibles: Departement, Employe, Projet, AffectationProjet");
        System.out.print("Entrez le nom de la table où vous voulez insérer des données: ");
        String tableName = scanner.nextLine();

        String[] columnNames = getColumnNames(con, tableName);

        StringBuilder insertSQL = new StringBuilder("INSERT INTO " + tableName + " (");

        // Ajout des colonnes dans la requête d'insertion
        for (int i = 0; i < columnNames.length; i++) {
            insertSQL.append(columnNames[i]);
            if (i < columnNames.length - 1) {
                insertSQL.append(", ");
            }
        }

        insertSQL.append(") VALUES (");

        // Ajout des valeurs dans la requête d'insertion
        for (int i = 0; i < columnNames.length; i++) {
            System.out.print("Entrez la valeur pour " + columnNames[i] + ": ");
            String value = scanner.nextLine();
            insertSQL.append("'").append(value).append("'");
            if (i < columnNames.length - 1) {
                insertSQL.append(", ");
            }
        }

        insertSQL.append(")");

        try (Statement stmt = con.createStatement()) {
            stmt.executeUpdate(insertSQL.toString());
            System.out.println("Données insérées avec succès !");
        }
    }

    // Fonction pour afficher les données d'une table
    public static void displayTableData(Connection con, Scanner scanner) throws SQLException {
        try (Statement stmt = con.createStatement()) {
            System.out.print("Entrez le nom de la table que vous voulez afficher: ");
            String tableName = scanner.nextLine();

            String selectSQL = "SELECT * FROM " + tableName;
            ResultSet rs = stmt.executeQuery(selectSQL);

            String[] columnNames = getColumnNames(con, tableName);

            System.out.println("Données de la table " + tableName + ":");
            while (rs.next()) {
                for (String columnName : columnNames) {
                    System.out.print(rs.getString(columnName) + "\t");
                }
                System.out.println();
            }

            rs.close();
        }
    }

    // Fonction pour supprimer des données d'une table
    public static void deleteData(Connection con, Scanner scanner) throws SQLException {
        System.out.print("Entrez le nom de la table où vous voulez supprimer des données: ");
        String tableName = scanner.nextLine();

        String[] columnNames = getColumnNames(con, tableName);

        System.out.println("Colonnes disponibles: ");
        for (String columnName : columnNames) {
            System.out.println(columnName);
        }

        System.out.print("Entrez le nom de la colonne pour la condition: ");
        String columnName = scanner.nextLine();

        System.out.print("Entrez la valeur pour la condition: ");
        String value = scanner.nextLine();

        String deleteSQL = "DELETE FROM " + tableName + " WHERE " + columnName + " = '" + value + "'";

        try (Statement stmt = con.createStatement()) {
            int rowsAffected = stmt.executeUpdate(deleteSQL);
            if (rowsAffected > 0) {
                System.out.println("Données supprimées avec succès !");
            } else {
                System.out.println("Aucune donnée correspondante trouvée.");
            }
        }
    }

    // Fonction pour récupérer les noms des colonnes d'une table
    public static String[] getColumnNames(Connection con, String tableName) throws SQLException {
        String query = "SELECT * FROM " + tableName + " LIMIT 1"; // Fetching a dummy row to get metadata
        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            int numColumns = rs.getMetaData().getColumnCount();
            String[] columnNames = new String[numColumns];

            for (int i = 1; i <= numColumns; i++) {
                columnNames[i - 1] = rs.getMetaData().getColumnName(i);
            }

            return columnNames;
        }
    }
}
