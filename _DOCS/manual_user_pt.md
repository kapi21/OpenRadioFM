# Manual do Usuário - OpenRadioFM v.4.8 Cloud_Server

Bem-vindo ao **OpenRadioFM v.4.8 Cloud_Server**, a evolução definitiva do rádio FM para unidades Android (Head Units). Esta versão introduz capacidades de streaming e gestão de logótipos na nuvem, otimizada para máxima estabilidade e desempenho.

---

## 1. Interface e Navegação

### 1.1 Modos de Ecrã (Layouts)
OpenRadioFM conta com dois designs principais:
- **V2 (Clássico Vertical):** Otimizado para ecrãs tipo tablet ou verticais.
- **V3 (Premium Horizontal):** Design panorâmico ideal para o tablier, com ícones de grande formato e efeito **"Glass Mode"**.
- **Como mudar:** Prima longamente o botão **LOC/DX** para alternar. A app reiniciará automaticamente.

### 1.2 Navegação de Favoritos (Hardware)
- **Favoritos:** Os botões centrais permitem saltar entre as suas estações memorizadas. Compatível com comandos de volante (K706/MT8163).
- **Busca (Seek):** Os botões exteriores realizam a busca automática de sinal.

---

## 2. Personalização Premium (Menu Secreto)

Prima longamente o botão de **Configuração (EQ)** para aceder:

### 2.1 Cores do Tema e Modo Noite
Escolha entre 10 esquemas de cores. Em **Modo Noite**, aplicar-se-á a cor **"Night Blue"** para melhorar a visibilidade noturna e reduzir a fadiga ocular.

### 2.2 Modo de Fundo (Glass Mode)
1. **Preto Puro:** Máximo contraste.
2. **Imagem background.png:** Carregue uma imagem personalizada de `/sdcard/RadioLogos/background.png`.
3. **Logótipo Dinâmico (Glass Mode):** O fundo gera-se automaticamente a partir do logótipo da estação.

---

## 3. Logótipos e Streaming Online [Novidade v4.7 Beta]

### 3.1 Servidor de Logótipos (Beta)
A app pode descarregar logótipos automaticamente do nosso servidor Supabase.
- **Reset de Cache:** Se um logótipo estiver incorreto ou quiser forçar uma recarga, prima longamente o ícone da **Nuvem Cloud**. Aparecerá a mensagem *"Cache da estação apagada"* e a informação será redefinida.
- **IMPORTANTE:** Atualmente o catálogo de logótipos está focado principalmente em **Espanha**, mas graças ao Crowdsourcing expande-se todos os dias.
- Pode ser ativado em *Definições Premium > Logótipos Online*.

### 3.2 Streaming Online (Beta)
- **Funcionalidade:** Permite ouvir a estação via internet se o sinal FM for fraco.
- **Estado:** Esta função está em fase de **testes**. O catálogo de streaming está focado atualmente em estações de **Espanha**.
- **Hardware:** Optimizou-se o motor MT8163 para evitar bloqueios ao comutar entre FM e Streaming.

### 3.3 Contribuição à Comunidade (Crowdsourcing)
- **Como ajudar:** Ative a opção *"Contribuir para a Comunidade"* nas Definições Premium.
- **Funcionamento:** Ao sintonizar uma estação com RDS estável, a app enviará de forma anónima a frequência e o código PI para o servidor para que outros utilizadores beneficiem dos logótipos HD na sua zona.

---

## 4. Gestão de Favoritos

### 4.1 Guardar e Carregar (.fav)
Use o botão do **Disquete (💾)** para exportar ou importar a sua lista de favoritos. Isto permite cópias de segurança ou mover a sua configuração entre dispositivos.

---

## 5. Configuração de Hardware

Se experimentar problemas de áudio ou sintonização, selecione o seu motor em *Definições de Hardware*:
- **HCN (K706):** Para unidades Vento/HCN.
- **Eonon/Topway (MT8163):** Optimizado para evitar bloqueios na v4.7.
- **QS6:** Para unidades Nanis/NWD.

---
**AVISO:** Esta é uma versão **BETA**. Algumas funções de servidor e streaming estão sob testes constantes.
*Desenvolvido com ❤️ por Jimmy80 para a comunidade Android Head Unit - v.4.8 Cloud_Server*
