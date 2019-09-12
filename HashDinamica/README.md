# HashDinamica
Implementação da Hash Dinâmica, estrutura de dados para indexamento, em Java.

## Como usar ?

Encontre [aqui a documentação da hash](https://htmlpreview.github.io/?https://github.com/axell-brendow/Indexing/blob/master/HashDinamica/doc/index.html).


Código de teste:

```Java
import java.io.File;

import hash.hashs.HashStringInt;

public class HashTest
{
    public static void main(String[] args)
    {
        new File("diretorio.dir").delete();
        new File("buckets.db").delete();

        HashStringInt hash = new HashStringInt("diretorio.dir", "buckets.db", 2);

        hash.inserir("a", 1);
        hash.inserir("b", 2);
        hash.inserir("c", 3);

        System.out.println(hash);
    }
}

```

### 1ª forma de usar, usando diretamente o código fonte:

Todos os comandos usados (Windows/PowerShell):

```PowerShell
echo 'import java.io.File;

import hash.hashs.HashStringInt;

public class test
{
    public static void main(String[] args)
    {
        new File("diretorio.dir").delete();
        new File("buckets.db").delete();

        HashStringInt hash = new HashStringInt("diretorio.dir", "buckets.db", 2);

        hash.inserir("a", 1);
        hash.inserir("b", 2);
        hash.inserir("c", 3);

        System.out.println(hash);
    }
}
' | Out-File -Encoding default test.java  # Envia um código de teste para o arquivo test.java

ls | select Name   # Coloque sua classe .java e a pasta hash do meu repositório juntas como abaixo:
ls .\hash\hashs  # Apenas mostra as classes .java dentro da pasta ./hash/hashs

javac -classpath "./hash/hashs/*;." test.java   <# Compile passando como classpath o caminho da pasta hash e o caminho do seu .java.

No meu caso, o .java está no próprio diretório onde estou, por isso usei o . (ponto). Caso você esteja num sistema unix, troque o ; (ponto e vírgula) por : (dois pontos) no classpath. #>

java -classpath "./hash/hashs/*;." test   # Execute sua classe com o mesmo classpath de compilação
```


### 2ª forma de usar, usando o arquivo hash.jar:

Primeiro, [baixe o arquivo hash.jar](https://github.com/axell-brendow/Indexing/raw/master/HashDinamica/hash.jar).

Os próximos passos estão descritos nas imagens a abaixo:

![Exemplo de código com compilação e execução](https://i.imgur.com/VcqsxoW.png)

Caso você não queira deixar o hash.jar na mesma pasta que a sua classe java, você pode seguir estes passos que são uma continuação da primeira imagem:

![Compilação e execução com o .jar na pasta lib](https://i.imgur.com/WgV4c0J.png)

Todos os comandos usados (Windows/PowerShell):

```PowerShell
echo 'import java.io.File;

import hash.hashs.HashStringInt;

public class test
{
    public static void main(String[] args)
    {
        new File("diretorio.dir").delete();
        new File("buckets.db").delete();

        HashStringInt hash = new HashStringInt("diretorio.dir", "buckets.db", 2);

        hash.inserir("a", 1);
        hash.inserir("b", 2);
        hash.inserir("c", 3);

        System.out.println(hash);
    }
}
' | Out-File -Encoding default test.java  # Envia um código de teste para o arquivo test.java

ls | select Name   # Coloque sua classe .java e o arquivo hash.jar na mesma pasta como abaixo:
javac -classpath hash.jar test.java   # Compile passando como classpath o caminho de hash.jar

java -classpath hash.jar test   # Execute sua classe com o mesmo classpath de compilação
ls | select Name   # Estado do diretório depois da compilação e execução
mkdir lib > $null   # Cria a pasta lib

mv .\hash.jar .\lib\hash.jar   # Move hash.jar para ela

tree /f   # Mostra a organização dos arquivos
javac -classpath ".\lib\*;." test.java   <# Compile passando como classpath o caminho de hash.jar e o caminho do seu .java.

No meu caso, o .java está no próprio diretório onde estou, por isso usei o . (ponto). Caso você esteja num sistema unix, troque o ; (ponto e vírgula) por : (dois pontos) no classpath. #>

java -classpath ".\lib\*;." test   # Execute sua classe com o mesmo classpath de compilação
```
