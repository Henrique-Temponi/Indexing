package hash;/* See the project's root for license information. */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

/*
TODO: IMPLEMENTAR FUNÇÃO QUE PERCORRE OS CAMPOS SERIALIZE DE UMA CLASSE
TODO:
- percorrer campos da classe
- checar os campos que estão com a anotação Serialize
    - checar o tipo do campo com a anotação
            (primitivos, classes personalizadas, classes nativas ou externas)
    - checar se o usuário passou valores pra anotação
        - toString opcional
        - gerar o toString
        - caso tenha passado, validar a existência dos métodos
            - numMaxBytes validar maior que 0
            - metodoLerBytes e metodoObterBytes devem coexistir

        - caso não tenha passado
            - numMaxBytes para strings terá um valor padrão, outros tipos não
                    primitivos não terão um valor padrão
            - caso não tenha passado metodoLerBytes ou metodoObterBytes, o tipo
                    do campo deve ser primitivo ou String ou deve ser uma classe
                    com anotações Serialize
*/

/**
 * Classe utilitária para manusear os registros de índices em um bucket.
 * Um registro é apenas uma tripla onde o primeiro elemento é a lápide,
 * o segundo a chave e o terceiro o dado: { lapide, chave, dado }.
 * 
 * @author Axell Brendow ( https://github.com/axell-brendow )
 *
 * @param <TIPO_DAS_CHAVES> Classe da chave. Caso essa classe não seja de um
 * tipo primitivo, ela deve ser filha da interface {@link Serializavel}.
 * @param <TIPO_DOS_DADOS> Classe do dado. Caso essa classe não seja de um
 * tipo primitivo, ela deve ser filha da interface {@link Serializavel}.
 */

public class Registro<TIPO_DAS_CHAVES, TIPO_DOS_DADOS> implements Serializavel
{
    public static final char ATIVADO = ' ';
    public static final char DESATIVADO = '*';
    public static int TAMANHO_MAXIMO_EM_BYTES_STRINGS = 300;

    protected char lapide;
    protected TIPO_DAS_CHAVES chave;
    protected TIPO_DOS_DADOS dado;
    protected short quantidadeMaximaDeBytesParaAChave;
    protected short quantidadeMaximaDeBytesParaODado;
    protected Class<TIPO_DAS_CHAVES> classeDaChave;
    protected Class<TIPO_DOS_DADOS> classeDoDado;
    protected Constructor<TIPO_DAS_CHAVES> construtorDaChave;
    protected Constructor<TIPO_DOS_DADOS> construtorDoDado;

    public static void erroClasseInvalida(Class<?> classe)
    {
        IO.printlnerr("ERRO: a classe " + classe.getName()
                + " não é de tipo primitivo, não implementa a interface "
                + "Serializavel, não contém anotações @Serialize e nem"
                + "implementa a interface java.io.Serializable");
    }

    public static int obterTamanhoDeUmSerializable(Class<?> classe)
    {
        int tamanho = -1;

        if (Serializable.class.isAssignableFrom(classe))
        {
            try
            {
                ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
                Object obj = classe.getDeclaredConstructor().newInstance();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(
                        byteOutputStream);

                objectOutputStream.writeObject(obj);
                objectOutputStream.flush();
                objectOutputStream.close();
                // Tenta estimar o tamanho por meio da serialização de um objeto
                tamanho = byteOutputStream.toByteArray().length;
            }

            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        return tamanho;
    }
    private Object tentarInvokar(Method metodo, Class<?> classe)
    {
        Object resultado = null;

        try
        {
            resultado = metodo.invoke(
                    classe.getDeclaredConstructor().newInstance());
        }

        catch (IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | InstantiationException
                | SecurityException | NoSuchMethodException e)
        {
            IO.printlnerr("ERRO: a classe " + classe.getName()
                    + " não é instanciável via construtor padrão.");
        }

        return resultado;
    }

    private Object tentarObterEInvocar(String nomeMetodo, Class<?> classe)
    {
        Object resultado = null;

        try
        {
            Method metodo = classe.getMethod(nomeMetodo);
            resultado = tentarInvokar(metodo, classe);
        }

        catch (NoSuchMethodException e)
        {
            IO.printlnerr("ERRO: o método " + nomeMetodo + " não existe na classe "
                    + classe.getName());
        }

        return resultado;
    }

    /**
     * Dada uma classe, caso ela seja de um tipo primitivo, obtém a quantidade de
     * bytes que esse primitivo gasta no Java. Caso ela seja de um tipo
     * {@link Serializavel}, obtém a quantidade máxima de bytes que esse
     * {@link Serializavel} gasta. Caso contrário, imprime um erro indicando que
     * a classe precisa ser de um tipo primitivo ou {@link Serializavel}.
     *
     * @param classe Classe do objeto que deseja-se obter a quantidade de bytes.
     *
     * @return a quantidade máxima de bytes que objetos dessa classe gastam.
     */
    private short obterTamanhoMaximoEmBytes(Class<?> classe)
    {
        int tamanho = -1;

        if (classe.equals(String.class)) tamanho = TAMANHO_MAXIMO_EM_BYTES_STRINGS;

        else if (Classes.isWrapper(classe) || Classes.isPrimitive(classe))
        {
            if (classe.equals(Boolean.class) || classe.equals(boolean.class))
                tamanho = 1;

            else if (classe.equals(Character.class) || classe.equals(char.class))
                tamanho = Character.BYTES;

            else if (classe.equals(Byte.class) || classe.equals(byte.class))
                tamanho = Byte.BYTES;

            else if (classe.equals(Short.class) || classe.equals(short.class))
                tamanho = Short.BYTES;

            else if (classe.equals(Integer.class) || classe.equals(int.class))
                tamanho = Integer.BYTES;

            else if (classe.equals(Long.class) || classe.equals(long.class))
                tamanho = Long.BYTES;

            else if (classe.equals(Float.class) || classe.equals(float.class))
                tamanho = Float.BYTES;

            else if (classe.equals(Double.class) || classe.equals(double.class))
                tamanho = Double.BYTES;

            else if (classe.equals(Void.class) || classe.equals(void.class))
                IO.printlnerr(
                        "ERRO: a classe " + classe.getName() + " é do tipo VOID.");

            else IO.printlnerr(
                    "ERRO: a classe " + classe.getName() + " é inválida.");
        }

        else if (Serializavel.class.isAssignableFrom(classe)) // Checa se a classe é Serializavel
        {
            try
            {
                tamanho = ((Serializavel) classe.getDeclaredConstructor()
                        .newInstance()).obterTamanhoMaximoEmBytes();
            }

            catch (InstantiationException | IllegalAccessException
                    | IllegalArgumentException | InvocationTargetException
                    | NoSuchMethodException | SecurityException e)
            {
                e.printStackTrace();
            }
        }

        else tamanho = obterTamanhoMaximoEmBytesComSerialize(classe);

        return (short) tamanho;
    }

    /**
     * Cria um objeto que gerencia um registro de indice no bucket da hash dinâmica.
     * 
     * @param lapide Lapide do registro.
     * @param chave Chave do registro.
     * @param dado Dado que corresponde à chave.
     * @param classeDaChave Construtor da chave. É necessário que a chave tenha um
     * construtor sem parâmetros.
     * @param classeDoDado Construtor do dado. É necessário que o dado tenha um
     * construtor sem parâmetros.
     */

    public Registro(char lapide, TIPO_DAS_CHAVES chave, TIPO_DOS_DADOS dado,
            Class<TIPO_DAS_CHAVES> classeDaChave, Class<TIPO_DOS_DADOS> classeDoDado)
    {
        this.lapide = lapide;
        this.chave = chave;
        this.dado = dado;
        this.classeDaChave = classeDaChave;
        this.classeDoDado = classeDoDado;

        try
        {
            this.construtorDaChave = Classes.isWrapper(classeDaChave)
                    ? classeDaChave.getConstructor(
                            Classes.wrapperParaPrimitivo.get(classeDaChave))
                    : classeDaChave.getConstructor();

            this.construtorDoDado = Classes.isWrapper(classeDoDado)
                    ? classeDoDado.getConstructor(
                            Classes.wrapperParaPrimitivo.get(classeDoDado))
                    : classeDoDado.getConstructor();

            this.quantidadeMaximaDeBytesParaAChave = obterTamanhoMaximoEmBytes(
                    classeDaChave);
            this.quantidadeMaximaDeBytesParaODado = obterTamanhoMaximoEmBytes(
                    classeDoDado);
        }

        catch (NoSuchMethodException | SecurityException
                | IllegalArgumentException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Calcula o tamanho que cada registro de indice gasta no bucket.
     * 
     * @return o tamanho que cada registro de indice gasta no bucket.
     */

    @Override
    public int obterTamanhoMaximoEmBytes()
    {
        int tamanho = 0;

        if (quantidadeMaximaDeBytesParaAChave > 0
                && quantidadeMaximaDeBytesParaODado > 0)
        {
            tamanho = Byte.BYTES + // tamanho da lapide
                    quantidadeMaximaDeBytesParaAChave + // tamanho da chave
                    quantidadeMaximaDeBytesParaODado; // tamanho do dado
        }

        return tamanho;
    }

    /**
     * Preenche com zeros a saída de dados até que o espaço
     * usado seja igual ao tamanho máximo.
     * 
     * @param dataOutputStream Corrente de saída de dados.
     * @param tamanhoUsado Quantidade de bytes já escritos.
     * @param tamanhoMaximo Tamanho máximo desejado.
     */

    private void completarEspacoNaoUsado(DataOutputStream dataOutputStream,
            int tamanhoUsado, int tamanhoMaximo)
    {
        int restante = tamanhoMaximo - tamanhoUsado;

        if (restante > 0)
        {
            byte[] lixo = new byte[restante];

            Arrays.fill(lixo, (byte) 0);

            try
            {
                dataOutputStream.write(lixo);
            }

            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * Obtém um arranjo com os bytes do objeto.
     *
     * @param objeto Objeto serializável.
     * @param classe Classe do objeto.
     *
     * @return um arranjo com os bytes do objeto.
     */
    private byte[] obterBytes(Object objeto, Class<?> classe)
    {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(
                byteArrayOutputStream);

        try
        {
            if (classe.equals(String.class))
                dataOutputStream.writeUTF((String) objeto);

            else if (Classes.isWrapper(classe) || Classes.isPrimitive(classe))
            {
                if (classe.equals(Boolean.class) || classe.equals(boolean.class))
                    dataOutputStream.writeBoolean((Boolean) objeto);

                else if (classe.equals(Character.class) || classe.equals(char.class))
                    dataOutputStream.writeChar((Character) objeto);

                else if (classe.equals(Byte.class) || classe.equals(byte.class))
                    dataOutputStream.writeByte((Byte) objeto);

                else if (classe.equals(Short.class) || classe.equals(short.class))
                    dataOutputStream.writeShort((Short) objeto);

                else if (classe.equals(Integer.class) || classe.equals(int.class))
                    dataOutputStream.writeInt((Integer) objeto);

                else if (classe.equals(Long.class) || classe.equals(long.class))
                    dataOutputStream.writeLong((Long) objeto);

                else if (classe.equals(Float.class) || classe.equals(float.class))
                    dataOutputStream.writeFloat((Float) objeto);

                else if (classe.equals(Double.class) || classe.equals(double.class))
                    dataOutputStream.writeDouble((Double) objeto);

                else if (classe.equals(Void.class) || classe.equals(void.class))
                    IO.printlnerr(
                            "ERRO: a classe " + classe.getName()
                                    + " é do tipo VOID.");

                else IO.printlnerr(
                        "ERRO: a classe " + classe.getName() + " é inválida.");
            }

            // Checa se a classe é Serializavel
            else if (Serializavel.class.isAssignableFrom(classe))
                dataOutputStream.write(((Serializavel) objeto).obterBytes());

            // TODO: IMPLEMENTAR LEITURA DAS ANOTAÇÕES

            else erroClasseInvalida(classe);
        }

        catch (IOException e)
        {
            e.printStackTrace();
        }

        return byteArrayOutputStream.toByteArray();
    }

    @Override
    public byte[] obterBytes()
    {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(
                byteArrayOutputStream);

        try
        {
            dataOutputStream.writeByte(lapide);

            byte[] byteArrayChave = obterBytes(chave, classeDaChave);
            int quantidadeDeBytes = byteArrayChave.length <= quantidadeMaximaDeBytesParaAChave
                    ? byteArrayChave.length
                    : quantidadeMaximaDeBytesParaAChave;

            dataOutputStream.write(byteArrayChave, 0, quantidadeDeBytes);

            completarEspacoNaoUsado(dataOutputStream, byteArrayChave.length,
                    quantidadeMaximaDeBytesParaAChave);

            byte[] byteArrayDado = obterBytes(dado, classeDoDado);
            quantidadeDeBytes = byteArrayDado.length <= quantidadeMaximaDeBytesParaODado
                    ? byteArrayDado.length
                    : quantidadeMaximaDeBytesParaODado;

            dataOutputStream.write(byteArrayDado, 0, quantidadeDeBytes);

            completarEspacoNaoUsado(dataOutputStream, byteArrayDado.length,
                    quantidadeMaximaDeBytesParaODado);

            dataOutputStream.close();
        }

        catch (IOException ioex)
        {
            ioex.printStackTrace();
        }

        return byteArrayOutputStream.toByteArray();
    }

    //	/**
    //	 * Atribui o campo "value" do objeto primitivo para o valor informado.
    //	 * Este método precisa também da classe do objeto primitivo.
    //	 *
    //	 * @param value Valor a ser atribuído ao campo.
    //	 * @param primitive Objeto de tipo primitivo que o campo será alterado.
    //	 * @param classe Classe desse tipo primitivo.
    //	 */
    //	private void setTo(Object value, Object primitive, Class<?> classe)
    //	{
    //		try
    //		{
    //			// Pega o campo "value" da classe. É esperado que essa classe seja
    //			// de um tipo primitivo pois todos eles têm o campo "value"
    //			Field field = classe.getDeclaredField("value");
    //			boolean acessibility = field.isAccessible();
    //
    //			// Se o campo é private ou protected, deixa-o acessível
    //			field.setAccessible(true);
    //			field.set(primitive, value); // Atribui o campo para o valor
    //			field.setAccessible(acessibility); // Volta a acessibilidade
    //		}
    //
    //		catch (NoSuchFieldException | IllegalAccessException e)
    //		{
    //			e.printStackTrace();
    //		}
    //	}

    /**
     * Faz com que o objeto leia o arranjo de bytes restaurando o seu estado.
     *
     * @param array Arranjo com os bytes do objeto.
     * @param nomeCampo Nome do campo desta classe que contém o objeto.
     * @param classe Classe do objeto.
     */
    @SuppressWarnings("all")
    private void lerBytes(byte[] array, String nomeCampo, Class<?> classe)
    {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(array);
        DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream);
        Field field;
        boolean accessibility;

        try
        {
            field = getClass().getDeclaredField(nomeCampo);

            accessibility = field.canAccess(this);
            field.setAccessible(true);

            if (classe.equals(String.class))
                field.set(this, dataInputStream.readUTF());

            else if (Classes.isWrapper(classe) || Classes.isPrimitive(classe))
            {
                if (classe.equals(Boolean.class) || classe.equals(boolean.class))
                    field.set(this, dataInputStream.readBoolean());

                else if (classe.equals(Character.class) || classe.equals(char.class))
                    field.set(this, dataInputStream.readChar());

                else if (classe.equals(Byte.class) || classe.equals(byte.class))
                    field.set(this, dataInputStream.readByte());

                else if (classe.equals(Short.class) || classe.equals(short.class))
                    field.set(this, dataInputStream.readShort());

                else if (classe.equals(Integer.class) || classe.equals(int.class))
                    field.set(this, dataInputStream.readInt());

                else if (classe.equals(Long.class) || classe.equals(long.class))
                    field.set(this, dataInputStream.readLong());

                else if (classe.equals(Float.class) || classe.equals(float.class))
                    field.set(this, dataInputStream.readFloat());

                else if (classe.equals(Double.class) || classe.equals(double.class))
                    field.set(this, dataInputStream.readDouble());

                else if (classe.equals(Void.class) || classe.equals(void.class))
                    IO.printlnerr(
                            "ERRO: a classe " + classe.getName()
                                    + " é do tipo VOID.");

                else IO.printlnerr(
                        "ERRO: a classe " + classe.getName() + " é inválida.");
            }

            // Checa se a classe é Serializavel
            else if (Serializavel.class.isAssignableFrom(classe))
                ((Serializavel) field.get(this)).lerBytes(array);

            // TODO: IMPLEMENTAR LEITURA DAS ANOTAÇÕES E IR SETANDO OS CAMPOS

            else erroClasseInvalida(classe);

            field.setAccessible(accessibility);
        }

        catch (NoSuchFieldException | IOException | IllegalAccessException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Cria uma nova instância do campo informado.
     *
     * @param nomeCampo Nome do campo nesta classe.
     * @param classe Classe do campo.
     */
    @SuppressWarnings("all")
    private void newInstance(String nomeCampo, Class<?> classe)
    {
        Field field;
        boolean accessibility;

        try
        {
            field = getClass().getDeclaredField(nomeCampo);
            accessibility = field.canAccess(this);
            field.setAccessible(true);

            if (classe.equals(String.class)) field.set(this, "");

            else if (Classes.isWrapper(classe) || Classes.isPrimitive(classe))
            {
                if (classe.equals(Boolean.class) || classe.equals(boolean.class))
                    field.set(this, false);

                else if (classe.equals(Character.class) || classe.equals(char.class))
                    field.set(this, 0);

                else if (classe.equals(Byte.class) || classe.equals(byte.class))
                    field.set(this, 0);

                else if (classe.equals(Short.class) || classe.equals(short.class))
                    field.set(this, 0);

                else if (classe.equals(Integer.class) || classe.equals(int.class))
                    field.set(this, 0);

                else if (classe.equals(Long.class) || classe.equals(long.class))
                    field.set(this, 0);

                else if (classe.equals(Float.class) || classe.equals(float.class))
                    field.set(this, 0);

                else if (classe.equals(Double.class) || classe.equals(double.class))
                    field.set(this, 0);

                else if (classe.equals(Void.class) || classe.equals(void.class))
                    IO.printlnerr(
                            "ERRO: a classe " + classe.getName()
                                    + " é do tipo VOID.");

                else IO.printlnerr(
                        "ERRO: a classe " + classe.getName() + " é inválida.");
            }

            // Checa se a classe é Serializavel
            else if (Serializavel.class.isAssignableFrom(classe))
                field.set(this, classe.getDeclaredConstructor().newInstance());

            else erroClasseInvalida(classe);

            field.setAccessible(accessibility);
        }

        catch (NoSuchFieldException | IllegalAccessException | InstantiationException
                | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void lerBytes(byte[] bytes)
    {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream);

        try
        {
            lapide = (char) dataInputStream.readByte();

            byte[] byteArrayChave = new byte[quantidadeMaximaDeBytesParaAChave];
            byte[] byteArrayDado = new byte[quantidadeMaximaDeBytesParaODado];

            dataInputStream.read(byteArrayChave);
            dataInputStream.read(byteArrayDado);

            newInstance("chave", classeDaChave);
            newInstance("dado", classeDoDado);

            lerBytes(byteArrayChave, "chave", classeDaChave);
            lerBytes(byteArrayDado, "dado", classeDoDado);

            byteArrayInputStream.close();
            dataInputStream.close();
        }

        catch (IllegalArgumentException | IOException e)
        {
            e.printStackTrace();
        }
    }

    public String toString(String delimitadorEntreOsCamposDoRegistro,
            boolean mostrarApenasAChave)
    {
        return mostrarApenasAChave ? chave.toString()
                : ("'" + lapide + "'" + delimitadorEntreOsCamposDoRegistro + chave
                        + delimitadorEntreOsCamposDoRegistro + dado);
    }

    @Override
    public String toString()
    {
        return toString(", ", false);
    }
}