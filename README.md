# JavaAppJEE

Application web academique de gestion des files d'attente pour un cabinet.

## Description

Ce projet permet de gerer une file d'attente dans un cabinet. Le patient peut s'inscrire, se connecter, choisir un cabinet, prendre un ticket et suivre sa position. L'administrateur peut ajouter des patients, consulter la liste d'attente, appeler le patient suivant et reinitialiser la file.

## Technologies

- Java JEE / Jakarta EE
- JSF
- PrimeFaces
- JDBC
- MySQL / MariaDB avec XAMPP
- Maven
- Tomcat 10

## Base de donnees

La configuration par defaut se trouve dans `src/main/java/com/queueweb/config/DatabaseConfig.java`.

- Host : `localhost`
- Port : `3000`
- Database : `queue_jee_app`
- User : `root`
- Password : vide

Demarrer MySQL dans XAMPP, puis importer le fichier SQL si necessaire :

```sql
database/queue_jee_app.sql
```

## Lancer le projet

Depuis le dossier `JavaAppJEE`, compiler le projet avec Maven :

```bash
mvn clean package
```

Le fichier genere sera :

```text
target/JavaAppJEE.war
```

Deployer ce fichier `.war` dans Tomcat 10, puis demarrer Tomcat.

## URLs

Apres le lancement de Tomcat, ouvrir :

```text
http://localhost:8080/JavaAppJEE/
```

Pages principales :

- `index.xhtml` : connexion / inscription patient
- `patient.xhtml` : espace patient
- `admin.xhtml` : espace administrateur
