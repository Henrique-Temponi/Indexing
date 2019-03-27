import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.function.BiFunction;
import java.util.function.DoubleFunction;
import java.util.function.Function;

/**
 * Classe que gerencia um bucket específico.
 * 
 * @author Axell Brendow ( https://github.com/axell-brendow )
 *
 * @param <TIPO_DAS_CHAVES> Classe da chave.
 * @param <TIPO_DOS_DADOS> Classe do dado.
 */

public class Bucket<TIPO_DAS_CHAVES extends Serializavel, TIPO_DOS_DADOS extends Serializavel> implements Serializavel
{
	private static final byte PROFUNDIDADE_LOCAL_PADRAO = 1;
	// o cabeçalho do bucket é apenas a profundidade local até o momento
	public static final int DESLOCAMENTO_CABECALHO = Byte.BYTES;
	
	private byte profundidadeLocal;
	private int numeroDeRegistrosPorBucket;
	RegistroDoIndice<TIPO_DAS_CHAVES, TIPO_DOS_DADOS> registroDoIndice;
	byte[] bucket;
	
	/**
	 * Cria um objeto que gerencia um bucket da hash dinâmica.
	 * 
	 * @param profundidadeLocal Profundidade local inicial.
	 * @param numeroDeRegistrosPorBucket Numero de registros por bucket caso o arquivo
	 * não tenha sido criado ainda.
	 * @param quantidadeMaximaDeBytesParaAChave Tamanho máximo que a chave pode gastar.
	 * @param quantidadeMaximaDeBytesParaODado Tamanho máximo que o dado pode gastar.
	 * @param construtorDaChave Construtor da chave. É necessário que a chave tenha um
	 * construtor sem parâmetros.
	 * @param construtorDoDado Construtor do dado. É necessário que o dado tenha um
	 * construtor sem parâmetros.
	 */
	
	public Bucket(
		byte profundidadeLocal,
		int numeroDeRegistrosPorBucket,
		short quantidadeMaximaDeBytesParaAChave,
		short quantidadeMaximaDeBytesParaODado,
		Constructor<TIPO_DAS_CHAVES> construtorDaChave,
		Constructor<TIPO_DOS_DADOS> construtorDoDado)
	{
		this.profundidadeLocal = profundidadeLocal;
		this.numeroDeRegistrosPorBucket = numeroDeRegistrosPorBucket;
		
		this.registroDoIndice =
			new RegistroDoIndice<>(
				'*', null, null,
				quantidadeMaximaDeBytesParaAChave,
				quantidadeMaximaDeBytesParaODado,
				construtorDaChave,
				construtorDoDado
			);
		
		iniciarBucket();
	}
	
	/**
	 * Cria um objeto que gerencia um bucket da hash dinâmica. A profundidade local
	 * inicial para o bucket será {@link #PROFUNDIDADE_LOCAL_PADRAO}.
	 * 
	 * @param numeroDeRegistrosPorBucket Numero de registros por bucket caso o arquivo
	 * não tenha sido criado ainda.
	 * @param quantidadeMaximaDeBytesParaAChave Tamanho máximo que a chave pode gastar.
	 * @param quantidadeMaximaDeBytesParaODado Tamanho máximo que o dado pode gastar.
	 * @param construtorDaChave Construtor da chave. É necessário que a chave tenha um
	 * construtor sem parâmetros.
	 * @param construtorDoDado Construtor do dado. É necessário que o dado tenha um
	 * construtor sem parâmetros.
	 */
	
	public Bucket(
		int numeroDeRegistrosPorBucket,
		short quantidadeMaximaDeBytesParaAChave,
		short quantidadeMaximaDeBytesParaODado,
		Constructor<TIPO_DAS_CHAVES> construtorDaChave,
		Constructor<TIPO_DOS_DADOS> construtorDoDado)
	{
		this(
			PROFUNDIDADE_LOCAL_PADRAO,
			numeroDeRegistrosPorBucket,
			quantidadeMaximaDeBytesParaAChave,
			quantidadeMaximaDeBytesParaODado,
			construtorDaChave,
			construtorDoDado);
	}
	
	/**
	 * Calcula o tamanho em bytes que um bucket gasta.
	 * 
	 * @return o tamanho em bytes que um bucket gasta.
	 */
	
	private int obterTamanhoDeUmBucket()
	{
		return Byte.BYTES + // tamanho da profundidade local
			numeroDeRegistrosPorBucket * registroDoIndice.obterTamanhoMaximoEmBytes();
	}
	
	/**
	 * Aloca o espaço máximo que um bucket pode gastar em bytes e já inicia os
	 * registros do bucket como desativados, além de colocar também a profundidade
	 * local do bucket no primeiro byte.
	 * 
	 * <p>Ilustração da estrutura de um bucket:</p>
	 * 
	 * <b>[ profundidade local (byte), registros... ({@link RegistroDoIndice}) ]</b>
	 */
	
	private void iniciarBucket()
	{
		bucket = new byte[obterTamanhoDeUmBucket()];
		bucket[0] = profundidadeLocal;
		
		int tamanhoDeUmRegistro = registroDoIndice.obterTamanhoMaximoEmBytes();
		int deslocamento = DESLOCAMENTO_CABECALHO;
		
		// desativa todos os registros no bucket
		for (int i = 0; i < numeroDeRegistrosPorBucket; i++)
		{
			bucket[deslocamento + i * tamanhoDeUmRegistro] = RegistroDoIndice.REGISTRO_DESATIVADO;
		}
	}
	
	/**
	 * Percorre todos os registros do bucket aplicando um método
	 * em cada um deles. Esse método deve retornar um valor inteiro
	 * que indica se o procedimento deve parar ou não. O retorno
	 * 0 indica que o processo deve continuar, qualquer retorno
	 * diferente termina o processo. O segundo parâmetro que o método
	 * recebe é o deslocamento em relação ao início do arranjo
	 * {@code bucket} em que o registro está.
	 * 
	 * @param metodo Método a ser aplicado em cada registro.
	 * 
	 * @return {@code true} se o método retornar {@code true} uma
	 * vez. Caso contrário, {@code false}.
	 */
	
	public int percorrerRegistros(
		BiFunction<RegistroDoIndice<TIPO_DAS_CHAVES, TIPO_DOS_DADOS>, Integer, Integer> metodo)
	{
		int condicao = 0;
		
		if (metodo != null)
		{
			int deslocamento = DESLOCAMENTO_CABECALHO;
			int tamanhoDeUmRegistro = registroDoIndice.obterTamanhoMaximoEmBytes();
			
			for (int i = 0; condicao == 0 && i < numeroDeRegistrosPorBucket; i++)
			{
				deslocamento += i * tamanhoDeUmRegistro;
				
				registroDoIndice.lerBytes(bucket, deslocamento);
				
				condicao = metodo.apply(registroDoIndice, deslocamento);
			}
		}
		
		return condicao;
	}
	
	/**
	 * Tenta inserir a chave e o dado no bucket.
	 * 
	 * @param chave Chave a ser inserida.
	 * @param dado Dado que corresponde à chave.
	 * 
	 * @return {@code 0} se o bucket estiver cheio;
	 * {@code -1} se o par (chave, dado) já existe;
	 * {@code 1} se tudo correr bem.
	 */
	
	protected int inserir(TIPO_DAS_CHAVES chave, TIPO_DOS_DADOS dado)
	{
		return percorrerRegistros(
			(registro, deslocamento) ->
			{
				int status = 0; // indica que o processo deve continuar
				
				if (registro.lapide == RegistroDoIndice.REGISTRO_DESATIVADO)
				{
					registro.lapide = RegistroDoIndice.REGISTRO_ATIVADO;
					registro.chave = chave;
					registro.dado = dado;
					
					registro.escreverObjeto(bucket, deslocamento);
					status = 1; // término com êxito, registro inserido
				}
				
				if (registro.lapide == RegistroDoIndice.REGISTRO_ATIVADO &&
					registro.chave.equals(chave) &&
					registro.dado.equals(dado))
				{
					status = -1; // término com problema, registro já existe
				}
				
				return status;
			}
		);
	}
	
	/**
	 * Procura todos os registros com uma chave específica e gera
	 * uma lista com os dados correspondentes a essas chaves.
	 * 
	 * @param chave Chave a ser procurada.
	 * 
	 * @return Uma lista com os dados correspondentes às chaves.
	 */
	
	public ArrayList<TIPO_DOS_DADOS> listarDadosComAChave(TIPO_DAS_CHAVES chave)
	{
		ArrayList<TIPO_DOS_DADOS> lista = new ArrayList<>();
		
		percorrerRegistros(
			(registro, deslocamento) ->
			{
				if (registro.chave.equals(chave))
				{
					lista.add(registro.dado);
				}
				
				return 0; // continuar processo
			}
		);
		
		return lista;
	}
	
	@Override
	public void escreverObjeto(RandomAccessFile correnteDeSaida)
	{
		if (correnteDeSaida != null)
		{
			byte[] bytes = obterBytes();
			
			try
			{
				correnteDeSaida.write(bytes);
			}
			
			catch (IOException ioex)
			{
				ioex.printStackTrace();
			}
		}
	}
	
	@Override
	public void lerObjeto(RandomAccessFile correnteDeEntrada)
	{
		if (correnteDeEntrada != null)
		{
			try
			{
				bucket = new byte[obterTamanhoDeUmBucket()];
				
				correnteDeEntrada.readFully(bucket);
				
				profundidadeLocal = bucket[0];
			}
			
			catch (IOException ioex)
			{
				ioex.printStackTrace();
			}
		}
	}
	
	@Override
	public byte[] obterBytes()
	{
		return bucket;
	}

	@Override
	public void escreverObjeto(byte[] correnteDeSaida, int deslocamento)
	{
		byte[] bytes = obterBytes();
		
		System.arraycopy(bytes, 0, correnteDeSaida, deslocamento, bytes.length);
	}

	@Override
	public void lerBytes(byte[] bytes, int deslocamento)
	{
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
		
		try
		{
			byteArrayInputStream.skip(deslocamento);
			
			byteArrayInputStream.read(bucket);
			
			byteArrayInputStream.close();
		}
		
		catch (IOException ioex)
		{
			ioex.printStackTrace();
		}
	}

	@Override
	public void lerBytes(byte[] bytes)
	{
		lerBytes(bytes, 0);
	}
}
