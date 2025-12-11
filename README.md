# Progress Quest - Java


Recriação simplificada da ideia de Progress Quest (jogo "zero-player") usando Java e POO.

## UML
https://www.mermaidchart.com/d/7bbe82da-48e2-4f5b-9092-d2565dd98ffc

## Como rodar

Pré-requisitos: JDK 17+ e Maven.


```bash
# clonar
git clone <https://github.com/ofranbraga/ProgressQuest>
cd ProgressQuest


# compilar
mvn -q clean package


# executar
java --module-path "C:\javafx\lib" --add-modules javafx.controls,javafx.fxml -cp target/classes com.progressquest.ui.MainApp

## UML
