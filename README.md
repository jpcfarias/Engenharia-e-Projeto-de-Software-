Pré-requisitos: Tenha o Java JDK (versão 11 ou superior) e o Apache Maven instalados e configurados no seu sistema (variáveis de ambiente JAVA_HOME e MAVEN_HOME/PATH).
Salvar os Arquivos: Crie a estrutura de diretórios e salve cada arquivo de código (.java) e o pom.xml nos locais corretos.
Abrir Terminal/Console: Navegue até o diretório raiz do projeto (todo-list-java).
Compilar e Executar Testes:

mvn clean install

    clean: Limpa compilações anteriores.
    install: Compila o código, executa os testes unitários e instala o JAR no repositório local do Maven. Você verá a saída dos testes no console. Se algum teste falhar, a build irá parar.

Executar a Aplicação (Console): Após o mvn clean install ter sido bem-sucedido, um JAR executável será criado no diretório target/. Execute-o com:

java -jar target/todo-list-java-1.0-SNAPSHOT-jar-with-dependencies.jar

(O nome do JAR pode variar ligeiramente dependendo da versão no pom.xml). Isso iniciará a interface de console.
Usar a Aplicação: Siga as instruções do menu no console para adicionar, listar, editar, marcar e excluir tarefas. As tarefas serão salvas no arquivo tarefas.json no mesmo diretório onde você executou o comando java -jar.
