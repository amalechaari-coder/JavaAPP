# JavaAppJEE

Application web JEE simple realisee avec :

- `Jakarta Servlet`
- `JSP`
- `JDBC`
- `MySQL`


- inscription et connexion patient
- choix d'un cabinet
- prise de ticket a distance
- affichage du numero de ticket
- affichage du nombre de patients avant
- notification simple quand le tour approche
- espace admin
- ajout de patient
- appel du patient suivant
- visualisation de la file d'attente
- reinitialisation de la file


## Base de donnees

Valeurs par defaut du projet :

- `DB_HOST=localhost`
- `DB_PORT=3000`
- `DB_NAME=queue_jee_app`
- `DB_USER=root`
- `DB_PASSWORD=` vide


## Driver JDBC

Place le driver MySQL ou MariaDB dans :

- `src/main/webapp/WEB-INF/lib/`

Exemple :

- `mysql-connector-j-9.x.x.jar`

## Structure

- `src/main/java` : code Java
- `src/main/webapp` : JSP, CSS, configuration web
- `database/queue_jee_app.sql` : script SQL

## Deployment simple

1. Demarre `MySQL` dans `XAMPP`
2. Importe `database/queue_jee_app.sql` dans phpMyAdmin
3. Copie le driver JDBC dans `WEB-INF/lib`
4. Deploye le projet sur `Tomcat 10`

## Pages

- `/home`
- `/patient`
- `/admin`
