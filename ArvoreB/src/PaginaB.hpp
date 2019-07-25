/**
 * @file PaginaB.hpp
 * @author Axell Brendow ( https://github.com/axell-brendow )
 * @brief Arquivo da classe PaginaB.
 * 
 * @copyright Copyright (c) 2019 Axell Brendow Batista Moreira
 */

#include "templates/tipos.hpp"
#include "templates/serializavel.hpp"
#include "helpersArvore.hpp"

#include <iostream>
#include <algorithm>

using namespace std;

namespace constantes
{
    static const file_pointer_type ptrNuloPagina = -1;
}

/**
 * @brief Classe com as características da página da árvore B.
 * 
 * <p>Caso for usar o método print() dá classe, é necessário que a chave e o dado
 * sejam tipos primitivos ou então o operador << deve ser sobrecarregado para que
 * seja possível inserir esses itens num ostream (output stream).</p>
 * 
 * @see [Sobrecarregando o operador <<](https://docs.microsoft.com/pt-br/cpp/standard-library/overloading-the-output-operator-for-your-own-classes?view=vs-2019)
 * 
 * @tparam TIPO_DAS_CHAVES Tipo da chave dos registros. <b>É necessário que a chave
 * seja um tipo primitivo ou então que a sua classe/struct herde de Serializavel e
 * tenha um construtor sem parâmetros.</b>
 * @tparam TIPO_DOS_DADOS Tipo do dado dos registros. <b>É necessário que o dado
 * seja um tipo primitivo ou então que a sua classe/struct herde de Serializavel e
 * tenha um construtor sem parâmetros.</b>
 */
template<typename TIPO_DAS_CHAVES, typename TIPO_DOS_DADOS>
class PaginaB : public Serializavel
{
protected:
    // ------------------------- Campos

    int numeroDeElementos;
    int maximoDeBytesParaAChave;
    int maximoDeBytesParaODado;
    int numeroDeChavesPorPagina;
    int ordemDaArvore;
    file_pointer_type endereco;

public:
    // ------------------------- Typedefs

    /**
     * @brief Padroniza o tipo da página da árvore. Typedefs dentro de classes ou
     * structs são considerados como boa prática em C++.
     */
    typedef PaginaB<TIPO_DAS_CHAVES, TIPO_DOS_DADOS> Pagina;

    /**
     * @brief Padroniza o tipo do par (chave, dado).
     */
    typedef struct Tripla<TIPO_DAS_CHAVES, TIPO_DOS_DADOS, file_pointer_type> Tripla;

    // ------------------------- Campos
    
    vector<TIPO_DAS_CHAVES> chaves;
    vector<TIPO_DOS_DADOS> dados;
    vector<file_pointer_type> ponteiros;

    // ------------------------- Construtores

    PaginaB() : numeroDeElementos(0), endereco(constantes::ptrNuloPagina) { }

    /**
     * @brief Constrói uma nova página com a ordem informada.
     * 
     * @param ordemDaArvore Ordem da árvore B (quantidade máxima de filhos por página).
     * @param maximoDeBytesParaAChave Quantidade máxima de bytes que a chave pode gastar
     * nos registros.
     * @param maximoDeBytesParaODado Quantidade máxima de bytes que o dado pode gastar
     * nos registros.
     */
    PaginaB(int ordemDaArvore,
        int maximoDeBytesParaAChave,
        int maximoDeBytesParaODado) :

        numeroDeElementos(0),
        maximoDeBytesParaAChave(maximoDeBytesParaAChave),
        maximoDeBytesParaODado(maximoDeBytesParaODado),
        numeroDeChavesPorPagina(ordemDaArvore - 1),
        ordemDaArvore(ordemDaArvore),
        endereco(constantes::ptrNuloPagina)
    {
        chaves.reserve(numeroDeChavesPorPagina);
        dados.reserve(numeroDeChavesPorPagina);
        ponteiros.reserve(ordemDaArvore);

        fill(ponteiros.begin(), ponteiros.end(), constantes::ptrNuloPagina);
    }

    /**
     * @brief Constrói uma nova página com a ordem informada e infere automaticamente
     * a quantidade de bytes que o TIPO_DAS_CHAVES e o TIPO_DOS_DADOS gasta.
     * 
     * @param ordemDaArvore Ordem da árvore B (quantidade máxima de filhos por página).
     */
    PaginaB(int ordemDaArvore) : PaginaB(ordemDaArvore, 0, 0)
    {
        obterTamanhoEmBytesDaChaveEDoDado<TIPO_DAS_CHAVES, TIPO_DOS_DADOS>(
            maximoDeBytesParaAChave, maximoDeBytesParaODado
        );
    }

    /**
     * @brief Constrói uma nova página a partir do vetor de bytes do DataInputStream.
     * 
     * @param bytes DataInputStream com o vetor de bytes da página.
     * @param ordemDaArvore Ordem da árvore B (quantidade máxima de filhos por página).
     * @param maximoDeBytesParaAChave Quantidade máxima de bytes que a chave pode gastar
     * nos registros.
     * @param maximoDeBytesParaODado Quantidade máxima de bytes que o dado pode gastar
     * nos registros.
     */
    PaginaB(DataInputStream& bytes, int ordemDaArvore) : PaginaB(ordemDaArvore)
    {
        lerBytes(bytes);
    }

    // ------------------------- Métodos herdados de Serializavel

    virtual int obterTamanhoMaximoEmBytes() override
    {
        // bytes para guardar a quantidade de elementos na página
        return sizeof( decltype(numeroDeElementos) ) + // falta link aqui
            ordemDaArvore * sizeof(file_pointer_type) + // bytes para os ponteiros
            numeroDeChavesPorPagina * maximoDeBytesParaAChave + // bytes para as chaves
            numeroDeChavesPorPagina * maximoDeBytesParaODado; // bytes para os dados
    }

    virtual DataOutputStream& gerarDataOutputStream(DataOutputStream& out) override
    {
        // É necessário que a chave e o dado sejam tipos primitivos ou que
        // herdem a classe Serializavel e tenham um construtor sem parâmetros.
        TIPO_DAS_CHAVES chave;
        TIPO_DOS_DADOS dado;

        out << numeroDeElementos << ponteiros.at(0);

        for (size_t i = 0; i < numeroDeElementos; i++)
        {
            chave = chaves.at(i);
            dado = dados.at(i);

            out << chave;
            out << dado;
            out << ponteiros.at(i + 1);
        }

        return out;
    }

    virtual void lerBytes(DataInputStream& input) override
    {
        // É necessário que a chave e o dado sejam tipos primitivos ou que
        // herdem a classe Serializavel e tenham um construtor sem parâmetros.
        TIPO_DAS_CHAVES chave;
        TIPO_DOS_DADOS dado;
        file_pointer_type ponteiro;

        input >> numeroDeElementos >> ponteiro;
        ponteiros[0] = ponteiro;
        
        for (size_t i = 0; i < numeroDeElementos; i++)
        {
            input >> chave;
            input >> dado;
            input >> ponteiro;

            chaves[i] = chave;
            dados[i] = dado;
            ponteiros[i + 1] = ponteiro;
        }
    }

    // ------------------------- Métodos

    file_pointer_type setEndereco(file_pointer_type endereco)
    {
        if (endereco > -1)
        {
            this->endereco = endereco;
        }
    }

    /**
     * @brief Retorna 0 (zero) caso a página não tenha sido carregada do arquivo ainda.
     * Caso contrário, retorna o seu número de elementos.
     * 
     * @return int O número de elementos da página.
     */
    int obterNumeroDeElementos()
    {
        return numeroDeElementos;
    }

    /**
     * @brief Obtém o endereço da página no arquivo.
     * 
     * @return file_pointer_type Endereço da página no arquivo.
     */
    file_pointer_type obterEndereco()
    {
        return endereco;
    }

    /**
     * @brief Checa se a página está sem elementos.
     * 
     * @return true Caso não haja elementos na página.
     * @return false Caso haja elementos na página.
     */
    bool vazia()
    {
        return numeroDeElementos == 0;
    }

    /**
     * @brief Checa se a página está cheia.
     * 
     * @return true Caso a página esteja cheia.
     * @return false Caso a página não esteja cheia.
     */
    bool cheia()
    {
        return numeroDeElementos == numeroDeChavesPorPagina;
    }

    /**
     * @brief Zera a quantidade de elementos e limpa todos os vetores internos.
     */
    void limpar()
    {
        numeroDeElementos = 0;
        endereco = constantes::ptrNuloPagina;

        chaves.clear();
        dados.clear();
        ponteiros.clear();
    }

    /**
     * @brief Calcula qual é o índice em que a chave deve ser inserida ou então
     * qual é o índice do ponteiro para descer na árvore.
     * 
     * @param chave Chave de pesquisa.
     * 
     * @return int Índice em que a chave deve ser inserida ou então
     * o índice do ponteiro para descer na árvore.
     */
    int obterIndiceDeDescida(TIPO_DAS_CHAVES chave)
    {
        // lower_bound (pesquisa binária) -> http://www.cplusplus.com/reference/algorithm/lower_bound/
        auto iteradorDeInsercao =
            lower_bound(chaves.begin(), chaves.begin() + chaves.size(), chave);

        return iteradorDeInsercao - chaves.begin();
    }

    /**
     * @brief Insere a tripla (chave, dado, ponteiro) na página no índice informado.
     * 
     * @param chave Chave do par.
     * @param dado Dado do par.
     * @param indiceDeInsercao Índice de inserção do par (chave, dado).
     * @param ponteiro Ponteiro à direita do par (chave, dado).
     * 
     * @return true Caso tudo corra bem.
     * @return false Caso a página esteja cheia.
     */
    bool inserir(TIPO_DAS_CHAVES chave, TIPO_DOS_DADOS dado, int indiceDeInsercao,
        file_pointer_type ponteiro = constantes::ptrNuloPagina)
    {
        bool sucesso = !cheia(); // Só é possível inserir se a página não estiver cheia

        if (sucesso)
        {
            chaves.insert(chaves.begin() + indiceDeInsercao, chave);
            dados.insert(dados.begin() + indiceDeInsercao, dado);
            ponteiros.insert(ponteiros.begin() + indiceDeInsercao + 1, ponteiro);

            numeroDeElementos++;
        }

        return sucesso;
    }
    
    /**
     * @brief Insere o par (chave, dado) na página de forma ordenada.
     * 
     * @param chave Chave do par.
     * @param dado Dado do par.
     * 
     * @return int -1 caso a inserção falhe (página cheia). Caso contrário,
     * o índice onde o par foi inserido.
     */
    int inserir(TIPO_DAS_CHAVES chave, TIPO_DOS_DADOS dado)
    {
        int indiceDeInsercao = obterIndiceDeDescida(chave);

        if (inserir(chave, dado, indiceDeInsercao))
        {
            indiceDeInsercao = -1;
        }

        return indiceDeInsercao;
    }

    /**
     * @brief Pega o par (chave, dado) no indiceLocal e a insere no indiceNoDestino
     * da paginaDestino. Após isso, remove o par e o seu desta página.
     * 
     * @param paginaDestino Página destino.
     * @param indiceNoDestino Índice de inserção na página destino.
     * @param indiceLocal Índice do par (chave, dado) nesta página.
     */
    void transferirElementoPara(Pagina* paginaDestino, int indiceNoDestino, int indiceLocal)
    {
        paginaDestino->inserir(
            chaves[indiceLocal], dados[indiceLocal],
            indiceNoDestino/* , ponteiros[indiceLocal + 1] */
        );

        chaves.erase(chaves.begin() + indiceLocal);
        dados.erase(dados.begin() + indiceLocal);
        // ponteiros.erase(ponteiros.begin() + indiceLocal + 1);
    }

    /**
     * @brief Caso a inserção do par (chave, dado) tenha acontecido na nova página,
     * o primeiro elemento dela deve ser promovido (o mais à esquerda). Caso a
     * inserção tenha acontecido na página que estava cheia, o último elemento dela
     * deve ser promovido (o mais à direita).
     * 
     * @param paginaDestino Página destino.
     * @param indice Índice de inserção na página destino.
     * @param exPaginaCheia Ponteiro para a página que estava cheia e provocou a
     * duplicação.
     * @param novaPagina Ponteiro para a nova página que foi criada na duplicação.
     * @param promoverElementoDoFim Caso seja true, promove o último elemento
     * desta página. Caso contrário, promove o primeiro.
     * @param arquivo Arquivo onde as páginas serão colocadas.
     */
    void promoverElementoPara(Pagina* paginaDestino, int indice, Pagina* exPaginaCheia,
        Pagina* novaPagina, bool promoverElementoDoFim, fstream& arquivo)
    {
        // Checa se a ex página cheia será afetada na promoção
        if (promoverElementoDoFim)
        {
            exPaginaCheia->ponteiros.erase(
                exPaginaCheia->ponteiros.begin() + exPaginaCheia->numeroDeElementos);

            transferirElementoPara(paginaDestino, indice, numeroDeElementos - 1);
        }

        else // A página nova será afetada na promoção
        {
            novaPagina->ponteiros.erase(novaPagina->ponteiros.begin());

            transferirElementoPara(paginaDestino, indice, 0);
        }

        numeroDeElementos--;

        // atualiza as páginas no arquivo
        exPaginaCheia->colocarNoArquivo(arquivo);
        novaPagina->colocarNoArquivo(arquivo);

        // Quando um elemento é promovido, o ponteiro da esquerda dele deve apontar
        // para a página que estava cheia (a que provocou a duplicação).
        // Já o ponteiro da direita deve apontar para a nova página (gerada pela
        // duplicação).
        paginaDestino->ponteiros[indice] = exPaginaCheia->endereco;
        paginaDestino->ponteiros[indice + 1] = novaPagina->endereco;
        paginaDestino->colocarNoArquivo(arquivo);
    }

    /**
     * @brief Transfere a quantidade de dados informada do containerFonte, da direita
     * para a esquerda, para o containerDestino.
     * 
     * @param containerDestino Destino.
     * @param containerFonte Fonte.
     * @param quantidade Quantidade de dados.
     */
    template<typename ContainerFonte, typename ContainerDestino>
    void transferirMetadePara(
        ContainerDestino& containerDestino,
        ContainerFonte& containerFonte, int quantidade)
    {
        auto inicio = containerFonte.end() - quantidade;
        auto fim = containerFonte.end();

        containerDestino.insert( // move para o destino usando iteradores sobre a fonte
            containerDestino.begin(),
            // move_iterator é usado para mover os elementos invés de copiá-los
            make_move_iterator(inicio),
            make_move_iterator(fim)
        );

        containerFonte.erase(inicio, fim);
    }

    /**
     * @brief Transfere metade das chaves, dos dados e dos ponteiros (com 1 a mais)
     * para outra página. Esses dados são pegos da direita para a esquerda.
     * 
     * @param paginaDestino Página destino.
     */
    void transferirMetadePara(Pagina* paginaDestino)
    {
        int quantidadeRemovida = numeroDeElementos / 2;
        
        transferirMetadePara(paginaDestino->chaves, chaves, quantidadeRemovida);
        transferirMetadePara(paginaDestino->dados, dados, quantidadeRemovida);
        transferirMetadePara(paginaDestino->ponteiros, ponteiros, quantidadeRemovida + 1);

        numeroDeElementos -= quantidadeRemovida;
        paginaDestino->numeroDeElementos += quantidadeRemovida;
    }

    /**
     * @brief Atualiza a página no arquivo caso ela já tenha um endereço. Caso
     * contrário, adiciona-a ao final do arquivo.
     * 
     * @param arquiv Arquivo onde a página deve ser colocada.
     * 
     * @return file_pointer_type constantes::ptrNuloPagina caso haja algum erro.
     * Caso contrário, retorna o endereço no qual a página foi colocada.
     */
    file_pointer_type colocarNoArquivo(fstream &arquivo)
    {
        if (endereco != constantes::ptrNuloPagina)
        {
            arquivo.seekp(endereco);
        }

        else
        {
            arquivo.seekp(0, fstream::end);
        }

        if (!arquivo.fail())
        {
            endereco = arquivo.tellp();
            arquivo << this;
        }

        return endereco;
    }

    /**
     * @brief Imprime, na output stream recebida, cada elemento da página gerando o
     * seguinte formato:
     * 
     * <p>
     * 1º ponteiro <delimitadorEntreOsItens> 1º chave <delimitadorEntreOsItens>
     * 1º dado <delimitadorEntreOsItens> 2º ponteiro <delimitadorEntreOsItens> ...
     * </p>
     * 
     * @param delimitadorEntreOsItens Delimitador entre cada elemento da página.
     * @param ostream Fluxo de saída onde a página será impressa.
     */
    void print(string delimitadorEntreOsItens = " ", ostream& ostream = cout)
    {
        if (!vazia())
        {
            ostream << ponteiros[0];

            for (size_t i = 0; i < numeroDeElementos; i++)
            {
                ostream << delimitadorEntreOsItens << chaves[i];
                ostream << delimitadorEntreOsItens << dados[i];
                ostream << delimitadorEntreOsItens << ponteiros[i + 1];
            }

            ostream << endl;
        }
    }
};

template<typename TIPO_DAS_CHAVES, typename TIPO_DOS_DADOS>
ostream& operator<<(ostream& ostream, PaginaB<TIPO_DAS_CHAVES, TIPO_DOS_DADOS>& pagina)
{
    pagina.print(" ", ostream);

    return ostream;
}

template<typename TIPO_DAS_CHAVES, typename TIPO_DOS_DADOS>
fstream& operator>>(fstream& fstream, PaginaB<TIPO_DAS_CHAVES, TIPO_DOS_DADOS>& pagina)
{
    pagina->setEndereco(fstream.tellg());
    Serializavel& serializavel = pagina;

    return fstream >> serializavel;
}
