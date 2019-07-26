/**
 * @file DataOutputStream.hpp
 * @author Axell Brendow ( https://github.com/axell-brendow )
 * @brief Classe de intermediação entre a sua variável e a saída de dados.
 * 
 * @copyright Copyright (c) 2019 Axell Brendow Batista Moreira
 */

#pragma once

#include "../templates/tipos.hpp"

#include <iostream>
#include <iterator>
#include <fstream>

using namespace std;

class DataOutputStream
{
private:
    // ------------------------- Campos

    /** Vetor de bytes onde serão guardados os dados. */
    vetor_de_bytes bytes;

public:
    // ------------------------- Construtores

    /**
     * @brief Constrói um novo objeto DataOutputStream com o vetor recebido.
     * 
     * @param vetor Vetor inicial.
     */
    DataOutputStream(vetor_de_bytes vetor) : bytes(vetor) { }

    /**
     * @brief Constrói um novo objeto DataOutputStream com um tamanho inicial
     * de buffer.
     * 
     * @param previsaoDaQuantidadeDeBytes Tamanho inicial do buffer.
     */
    DataOutputStream(int previsaoDaQuantidadeDeBytes) :
        DataOutputStream( vetor_de_bytes() )
    {
        // .reserve() garante mais espaço mas altera o endereço do vetor na memória
        bytes.reserve(previsaoDaQuantidadeDeBytes);
    }

    /**
     * @brief Constrói um novo objeto DataOutputStream com um tamanho inicial
     * de buffer de 16 bytes.
     */
    DataOutputStream() : DataOutputStream(16) {}

    // ------------------------- Métodos

    /**
     * @brief Checa se este fluxo está vazio.
     * 
     * @return true Retorna true caso este fluxo esteja vazio.
     * @return false Retorna false caso este fluxo não esteja vazio.
     */
    bool empty()
    {
        return bytes.empty();
    }

    /**
     * @brief Obtém um iterador que aponta para o primeiro byte deste fluxo.
     * 
     * @return iterador Retorna um iterador que aponta para o primeiro byte
     * deste fluxo.
     */
    iterador begin()
    {
        return bytes.begin();
    }

    /**
     * @brief Obtém um iterador que aponta para o último byte deste fluxo.
     * 
     * @return iterador Retorna um iterador que aponta para o último byte
     * deste fluxo.
     */
    iterador end()
    {
        return bytes.end();
    }

    size_t size()
    {
        return bytes.size();
    }

    size_t capacity()
    {
        return bytes.capacity();
    }

    DataOutputStream& resize(int size)
    {
        bytes.resize(size);

        return *this;
    }

    iterador obterCursor()
    {
        return begin() + size();
    }

    vetor_de_bytes obterVetor()
    {
        return bytes;
    }

    /**
     * @brief Copia bytes a partir do iterador de início até o de fim.
     *
     * @tparam Iterador Tipo do iterador.
     * @param inicio Iterador sobre o primeiro byte do valor a ser escrito.
     * @param fim Iterador sobre a posição após o último byte do valor a ser escrito.
     *
     * @return DataOutputStream& Retorna uma referência para este objeto.
     */
    template<typename Iterador>
    DataOutputStream& escreverPorIterador(Iterador inicio, Iterador fim)
    {
        // como inicio aponta para o primeiro byte do valor e fim para uma posição
        // após o último byte, o .insert() copia todos bytes do valor e guarda no
        // vetor deste objeto.
        bytes.insert(obterCursor(), inicio, fim);

        return *this; // retorna uma referência para este objeto.
    }
    
    /**
     * @brief Copia bytes do ponteiro recebido para o vetor deste objeto.
     * 
     * @tparam tipo Tipo do que se deseja escrever.
     * @param ptrValor Ponteiro para o primeiro byte do valor a ser escrito.
     * @param tamanhoDoValor Quantidade de bytes a serem escritos.
     * 
     * @return DataOutputStream& Retorna uma referência para este objeto.
     */
    template<typename tipo>
    DataOutputStream& escreverPorPonteiro(tipo *ptrValor, int tamanhoDoValor = sizeof(tipo))
    {
        // reinterpret_cast faz a conversão do ponteiro para "tipo_byte *".
        // Isso é feito para que eu possa iterar sobre os bytes do valor.
        tipo_byte *inicio = reinterpret_cast<tipo_byte *>(ptrValor);
        
        return escreverPorIterador(inicio, inicio + tamanhoDoValor);
    }

    /**
     * @brief Escreve tipos primitivos e objetos com tamanho pré definido no
     * vetor deste objeto.
     * 
     * @tparam tipo Tipo do que se deseja escrever.
     * @param valor Valor que se deseja escrever.
     * @param tamanhoDoValor Tamanho em bytes que o valor gasta.
     * 
     * @return DataOutputStream& Retorna uma referência para este objeto.
     */
    template<typename tipo>
    DataOutputStream& escrever(tipo& valor, int tamanhoDoValor = sizeof(tipo))
    {
        return escreverPorPonteiro(&valor);
    }

    /**
     * @brief Escreve uma string no vetor deste objeto. As strings têm um tratamento
     * especial pois é necessário escrever primeiro o tamanho delas antes de
     * escrever os seus caracteres.
     * 
     * @param str String a ser escrita.
     * 
     * @return DataOutputStream& Retorna uma referência para este objeto.
     */
    DataOutputStream& escreverString(string& str)
    {
        str_size_type tamanho = str.length();

        escrever(tamanho); // Escreve primeiro a quantidade de bytes que a string gasta

        return escreverPorPonteiro(const_cast<char *>( str.c_str() ), tamanho); // Agora, escreve a string
    }

    /**
     * @brief Escreve os dados de um DataOutputStream no vetor deste objeto.
     *
     * @param out DataOutputStream a ser mesclado com o atual.
     *
     * @return DataOutputStream& Retorna uma referência para este objeto.
     */
    DataOutputStream& escreverDataOutputStream(DataOutputStream& out)
    {
        return escreverPorIterador(out.begin(), out.obterCursor());
    }
};

// ------------------------- Operadores

template<typename tipo>
DataOutputStream& operator<<(DataOutputStream& dataOutputStream, tipo variavel)
{
    return dataOutputStream.escrever(variavel);
}

DataOutputStream& operator<<(DataOutputStream& dataOutputStream, string& variavel)
{
    return dataOutputStream.escreverString(variavel);
}

DataOutputStream& operator<<(DataOutputStream& dataOutputStream, const char *variavel)
{
    string str(variavel);

    return dataOutputStream << str;
}

DataOutputStream& operator<<(
    DataOutputStream& dataOutputStream,
    DataOutputStream& variavel)
{
    return dataOutputStream.escreverDataOutputStream(variavel);
}

// ------------------------- OutputStream e DataOutputStream

ostream& operator<<(ostream& ostream, DataOutputStream& out)
{
    if (out.capacity() > 0)
    {
        // Essa instância de iterador do tipo ostream_iterador tem a peculiaridade de
        // que ela sempre escreve o que for solicitado e logo em seguida escreve
        // um delimitador. No caso será uma vírgula.
        // Ex.:
        // ostream_iterator<int> myiter(cout, ","); // declara um iterador sobre cout
        // *myiter = 100 // imprime "100" e depois "," resultando em "100,"
        copy( out.begin(), out.end() - 1, ostream_iterator<int>(ostream, ",") );
        cout << (int) *out.end();
    }

    return ostream << endl;
}

// ------------------------- FileStream e DataOutputStream

fstream& operator<<(fstream& fstream, DataOutputStream& out)
{
    fstream.write( reinterpret_cast<char *>( out.begin().base() ), out.capacity() );

    return fstream;
}
