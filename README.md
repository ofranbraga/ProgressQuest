# Progress Quest - Java


Recriação simplificada da ideia de Progress Quest (jogo "zero-player") usando Java e POO.


## Como rodar


Pré-requisitos: JDK 17+ e Maven.


```bash
# clonar
git clone <repo-url>
cd progress-quest-java


# compilar
mvn -q clean package


# executar
mvn -q exec:java -Dexec.mainClass="com.progressquest.Main"