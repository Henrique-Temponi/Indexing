/**
 * @file PaginaB.cpp
 * @author Axell Brendow ( breno.axel@gmail.com ) ( https://github.com/axell-brendow )
 * @brief Arquivo para testes
 * 
 * @copyright Copyright (c) 2019 Axell Brendow Batista Moreira
 */

#include "tipos.hpp"

#include <iostream>

using namespace std;

template<typename TIPO_DAS_CHAVES, typename TIPO_DOS_DADOS>
class PaginaB
{
    private:
        vetor_de_bytes bytesDaPagina; // guardará os dados da página que vierem do arquivo
        int numeroDeElementos;
        vector<TIPO_DAS_CHAVES> chaves;
        vector<TIPO_DOS_DADOS> dados;

    public:
        PaginaB(vetor_de_bytes byteArray) : byteArray(byteArray) {}
};

template<typename TIPO_DAS_CHAVES, typename TIPO_DOS_DADOS>
ostream& operator<< (ostream &stream, PaginaB<TIPO_DAS_CHAVES, TIPO_DOS_DADOS> &pagina)
{
    return stream << pagina;
}
