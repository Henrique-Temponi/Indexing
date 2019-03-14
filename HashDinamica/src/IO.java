import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

/**
 * @author Axell Brendow ( https://github.com/axell-brendow )
 */

public class IO
{
	/**
	 * Abre o arquivo {@code fileName} no modo de acesso {@code mode}
	 * 
	 * @param fileName Nome do arquivo a ser aberto.
	 * @param mode Modo de acesso ("r", "w", "rw", "rws", "rwd").
	 * 
	 * @return {@code null} se alguma coisa falhar. Caso contrário,
	 * o {@link java.io.RandomAccessFile} correspendente com o
	 * arquivo aberto.
	 * 
	 * @see java.io.RandomAccessFile#RandomAccessFile(java.io.File, String)
	 */
	
	public static RandomAccessFile openFile(String fileName, String mode)
	{
		RandomAccessFile file = null;
		
		try
		{
			file = new RandomAccessFile(fileName, mode);
		}
		
		catch (FileNotFoundException fnfe)
		{
			fnfe.printStackTrace();
		}
		
		return file;
	}
}
