#include "ArvoreB/ArvoreB.hpp"

#include <iostream>
#include <cstring>

using namespace std;

int main()
{
    string fileName("TesteArvore.txt");
    
    ArvoreB<int, float> arvore(fileName, 4);

    arvore.inserir(1000 , 1000.5);
    arvore.inserir(2000 , 2000.5);
    arvore.inserir(3000 , 3000.5);
    arvore.inserir(200  , 200.5 );
    arvore.inserir(400  , 400.5 );
    arvore.inserir(1500 , 1500.5);
    arvore.inserir(600  , 600.5 );
    arvore.inserir(50   , 50.5  );
    arvore.inserir(12   , 12.5  );
    arvore.inserir(4    , 4.5   );

    arvore.mostrar();

    return 0;
}