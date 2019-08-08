#include "ArvoreBMais/ArvoreBMais.hpp"

using namespace std;

int main()
{
    ArvoreBMais<int, float> arvore("TesteArvore.txt", 4);

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
    arvore.mostrarPre();

    return 0;
}
