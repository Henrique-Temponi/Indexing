import java.io.RandomAccessFile;

/**
 * Interface para padronização da forma de serializar objetos.
 * 
 * @author Axell Brendow ( https://github.com/axell-brendow )
 * 
 * @see <a href="https://pt.wikipedia.org/wiki/Serializa%C3%A7%C3%A3o">
 * Serialização – Wikipédia, a enciclopédia livre</a>
 */

public interface Serializavel
{
	/**
	 * Escreve os bytes do objeto na {@code correnteDeSaida}
	 * a partir de onde o cursor dela estiver.
	 * 
	 * @param correnteDeSaida Corrente de saída dos bytes.
	 */
	
	void escreverObjeto(RandomAccessFile correnteDeSaida);
	
	/**
	 * Lê os bytes do objeto da {@code correnteDeEntrada}
	 * a partir de onde o cursor dela estiver.
	 * 
	 * @param correnteDeEntrada Corrente de entrada dos bytes.
	 */
	
	void lerObjeto(RandomAccessFile correnteDeEntrada);
	
	/**
	 * Gera um vetor com os bytes do objeto.
	 * 
	 * @return um vetor com os bytes do objeto.
	 */
	
	byte[] obterBytes();
	
	/**
	 * Escreve os bytes do objeto na {@code correnteDeSaida}
	 * a partir de um deslocamento.
	 * 
	 * @param correnteDeSaida Corrente de saída dos bytes.
	 */
	
	void escreverObjeto(byte[] correnteDeSaida, int deslocamento);
	
	/**
	 * Lê os bytes do objeto do vetor a partir de um
	 * deslocamento.
	 * 
	 * @param bytes Vetor com os bytes do objeto.
	 * @param offsetFromStart Deslocamento em relação ao início.
	 */
	
	void lerBytes(byte[] bytes, int deslocamento);
	
	/**
	 * Lê os bytes do objeto do vetor.
	 * 
	 * @param bytes Vetor com os bytes do objeto.
	 */
	
	void lerBytes(byte[] bytes);
}
