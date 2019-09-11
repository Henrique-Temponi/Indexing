# HashDinamica
Implementação da Hash Dinâmica, estrutura de dados para indexamento, em Java.

### Como usar ?

Encontre [aqui a documentação da hash](https://htmlpreview.github.io/?https://github.com/axell-brendow/Indexing/blob/master/HashDinamica/doc/index.html).

Primeiro, [baixe o arquivo hash.jar](https://github.com/axell-brendow/Indexing/raw/master/HashDinamica/hash.jar).

Os próximos passos estão descritos nas imagens a abaixo:

![Exemplo de código com compilação e execução](https://i.imgur.com/IJQ0zg0.png)

Caso você não queira deixar o hash.jar na mesma pasta que a sua classe java, você pode seguir estes passos que são uma continuação da primeira imagem:

![Compilação e execução com o .jar na pasta lib](https://i.imgur.com/JOUiL1l.png)

Todos os comandos usados:

```PowerShell
ls | select Name   # coloque sua classe .java e o arquivo hash.jar na mesma pasta como abaixo:
javac -classpath hash.jar HashTest.java   # compile passando como classpath o caminho de hash.jar

java -classpath hash.jar HashTest   # execute sua classe com o mesmo classpath de compilação
ls | select Name   # estado do diretório depois da compilação e execução
mkdir lib > $null   # cria a pasta lib

mv .\hash.jar .\lib\hash.jar   # move hash.jar para ela

tree /f   # mostra a organização dos arquivos
javac -classpath ".\lib\*;." HashTest.java   <# Compile passando como classpath o caminho de hash.jar e o caminho do seu .java.

No meu caso, o .java está no próprio diretório onde estou, por isso usei o . (ponto). Caso você esteja num sistema unix, troque o ; (ponto e vírgula) por : (dois pontos) no classpath. #>

java -classpath ".\lib\*;." HashTest   # execute sua classe com o mesmo classpath de compilação
```

Código de teste:

```Java
import java.io.File;

import hash.hashs.HashDinamicaStringInt;

public class HashTest
{
    public static void main(String[] args)
    {
        try
        {
            new File("diretorio.dir").delete();
            new File("buckets.db").delete();

            HashDinamicaStringInt hash = new HashDinamicaStringInt("diretorio.dir", "buckets.db", 2);

            hash.inserir("a", 1);
            hash.inserir("b", 2);
            hash.inserir("c", 3);
    
            System.out.println(hash);
        }
        
        catch (SecurityException | NoSuchMethodException e)
        {
            e.printStackTrace();
        }
    }
}

```