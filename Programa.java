/*
    Aluno: Vitor Trentim Navarro de Almeida
    18/04/2020
*/

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.sql.*;
import java.util.*;
import javax.swing.table.AbstractTableModel;

public class Programa extends JFrame implements ActionListener {
    Connection conexao;
    Statement stmt;
    JDesktopPane janelaPrincipal = new JDesktopPane();

    JMenuBar menuBarra = new JMenuBar();
    JMenu menuBD = new JMenu("Banco de Dados");
    JMenu menuTabela = new JMenu();

    JMenuItem itemCriar = new JMenuItem("Criar Tabela", KeyEvent.VK_C);
    JMenuItem itemDeletar = new JMenuItem("Deletar Tabela", KeyEvent.VK_D);
    JMenuItem itemSelecionarTabela = new JMenuItem("Selecionar Tabela", KeyEvent.VK_S);
    JMenuItem itemFechar = new JMenuItem("Fechar");

    JanelaCriar janela_criar;
    JanelaSelecionarTabela janela_selecionar;
    JanelaDeletarTabela janela_deletar;

    boolean isCriarOpen = false;
    boolean isDeletarOpen = false;
    boolean isSelecionarOpen = false;

    int tabelasAbertas = 0;
    ArrayList<JanelaManipularTabela> listaTabelasAbertas = new ArrayList<JanelaManipularTabela>();
    ArrayList<String> opcoes = new ArrayList<String>();
    public Programa(){
            // JANELA PRINCIPAL //
        super("Programa - Gestao de Banco de Dados");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        janelaPrincipal.setBackground(new Color(225, 225, 255));
        add(janelaPrincipal); //adiciona o JDektopPane ao JFrame
            // MENU //
        itemCriar.addActionListener(this); //CRIAR
        itemDeletar.addActionListener(this); //DELETAR
        itemSelecionarTabela.addActionListener(this); //SELECIONAR
        itemFechar.addActionListener(this); //FECHAR
        itemCriar.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
        itemDeletar.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK));
        itemSelecionarTabela.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        itemFechar.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK));

        itemCriar.setBackground(new Color(190, 210, 255));
        itemDeletar.setBackground(new Color(190, 210, 255));
        itemSelecionarTabela.setBackground(new Color(190, 210, 255));
        itemFechar.setBackground(new Color(190, 210, 255));

        menuBD.add(itemCriar);
        menuBD.add(itemDeletar);
        menuBD.add(itemSelecionarTabela);
        menuBD.add(new JSeparator()); //linha horizontal de divisao
        menuBD.add(itemFechar);
        menuBD.setMnemonic(KeyEvent.VK_B); //alt+B para abrir o menu "Banco de Dados"
        menuBarra.add(menuBD); //adiciona o menuBD ao JMenuBar
        menuBarra.setBackground(new Color(190, 210, 255));
        setJMenuBar(menuBarra);
            //   //
        abreBancoDeDados();
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == itemCriar) {
            if (isCriarOpen){
                janela_criar.dispose();
                janelaPrincipal.remove(janela_criar);
                revalidate();
            }
            if (isDeletarOpen){
                janela_deletar.dispose();
            }
            if (isSelecionarOpen){
                janela_selecionar.dispose();
            }
            janela_criar = new JanelaCriar();
            janelaPrincipal.add(janela_criar);
            janela_criar.toFront();
        } else if (e.getSource() == itemDeletar){
            if (tabelasAbertas != 0){
                JOptionPane.showMessageDialog(janelaPrincipal, "Necessario fechar todas as tabelas para abrir o menu Deletar.\n", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (isDeletarOpen){
                janela_deletar.dispose();
                janelaPrincipal.remove(janela_deletar);
                revalidate();
            }
            if (isCriarOpen){
                janela_criar.dispose();
            }
            if (isSelecionarOpen){
                janela_selecionar.dispose();
            }
            janela_deletar = new JanelaDeletarTabela();
            janelaPrincipal.add(janela_deletar);
            janela_deletar.toFront();
        } else if (e.getSource() == itemSelecionarTabela) {
            if (isSelecionarOpen) {
                janela_selecionar.dispose();
                janelaPrincipal.remove(janela_selecionar);
                revalidate();
            }
            if (isCriarOpen){
                janela_criar.dispose();
            }
            if (isDeletarOpen){
                janela_deletar.dispose();
            }
            janela_selecionar = new JanelaSelecionarTabela();
            janelaPrincipal.add(janela_selecionar);
            janela_selecionar.toFront();
        } else if (e.getSource() == itemFechar) {
            System.exit(0);
        }
    }

    void abreBancoDeDados() {
        try {
            Class.forName("org.hsql.jdbcDriver");
            conexao = DriverManager.getConnection("jdbc:HypersonicSQL:hsql://localhost:8080", "sa", "");
            stmt = conexao.createStatement();
        } catch (ClassNotFoundException ex) {
            JOptionPane.showMessageDialog(null, "O driver do banco de dados não foi encontrado.\n"+ex, "Erro", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Erro na iniciação do acesso ao banco de dados\n"+ex, "Erro", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    void atualizaOpcoes(){
        try {
            opcoes.clear();
            DatabaseMetaData md = conexao.getMetaData();
            ResultSet rs = md.getTables(null, null, "%", null);
            String str;
            while (rs.next()) {
                str = rs.getString(3);
                opcoes.add(str);
            }
            Collections.sort(opcoes);
        }catch (SQLException ex) {
            JOptionPane.showMessageDialog(janelaPrincipal, "Erro em operacao SQL.\n", "Erro", JOptionPane.ERROR_MESSAGE);
        } catch (NullPointerException ex) {
            JOptionPane.showMessageDialog(janelaPrincipal, "Problema interno (null pointer).\n", "Erro", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex){
            JOptionPane.showMessageDialog(janelaPrincipal, "Problema indefinido.\n", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
    ////////// ////////// ////////// ////////// ////////////////// ////////// ////////// ////////// //////////
    ////////// ////////// ////////// ////////// Classes de Janelas ////////// ////////// ////////// //////////
    ////////// ////////// ////////// ////////// ////////////////// ////////// ////////// ////////// //////////

    class JanelaCriar extends JInternalFrame implements ActionListener {
        JLabel titulo = new JLabel("Qual o tipo de tabela deseja criar?");

        JButton bCadastroClientes = new JButton("Cadastro de clientes");
        JButton bCadastroProdutos = new JButton("Cadastro de produtos");
        JButton bCadastroFornecedores = new JButton("Cadastro de Fornecedores");

        JButton bCriar = new JButton("Criar");
        JTextField entradaNome = new JTextField(20);
        JLabel nomeLabel = new JLabel("Nome da tabela: ");
        JLabel tipoTabela = new JLabel();
        String nomeTabela, complementoTabela, nomeArquivoTabela;
        Statement stmt;

        int tipoTabelaNum = -1;

        public JanelaCriar (){
            super("Criar Tabela", false, true, false, true);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setLayout(new GridLayout(12,1,2,2));
            setBackground(new Color(220, 255, 230));
            isCriarOpen = true;

            addInternalFrameListener(new InternalFrameAdapter(){ //Listener para saber quando as janelas de tabela são fechadas
                @Override
                public void	internalFrameClosed(InternalFrameEvent e){
                    isCriarOpen = false;
                }
            });

            bCadastroClientes.addActionListener(this);
            bCadastroProdutos.addActionListener(this);
            bCadastroFornecedores.addActionListener(this);
            bCriar.addActionListener(this);
            bCriar.setBackground(new Color(100,255,100));

            add(new JSeparator());
            add(titulo);
            add(bCadastroClientes);
            add(bCadastroProdutos);
            add(bCadastroFornecedores);
            add(new JSeparator());
            add(new JSeparator());
            add(tipoTabela);
            add(nomeLabel);
            add(entradaNome);
            add(bCriar);
            add(new JSeparator());

            getRootPane().setDefaultButton(bCriar);

            tipoTabela.setVisible(false);
            nomeLabel.setVisible(false);
            entradaNome.setVisible(false);
            bCriar.setVisible(false);
            setVisible(true);
            setBounds(0,0,250,350);
        }

        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == bCadastroClientes){
                tipoTabelaNum = 1;
                complementoTabela = "(NOME VARCHAR(30), CPF BIGINT, RG BIGINT, TELEFONE BIGINT, ENDERECO VARCHAR(80))";
                tipoTabela.setText("Nova tabela de CLIENTES:");
                tipoTabela.setVisible(true);
                nomeLabel.setVisible(true);
                entradaNome.setVisible(true);
                entradaNome.requestFocus();
                bCriar.setVisible(true);
                revalidate();
            }
            else if (e.getSource() == bCadastroProdutos){
                tipoTabelaNum = 2;
                complementoTabela = "(PRODUTO VARCHAR(30), ID BIGINT, PRECO REAL, QUANTIDADE INTEGER, DEPARTAMENTO VARCHAR(30))";
                tipoTabela.setText("Nova tabela de PRODUTOS:");
                tipoTabela.setVisible(true);
                nomeLabel.setVisible(true);
                entradaNome.setVisible(true);
                bCriar.setVisible(true);
                revalidate();
            }
            else if (e.getSource() == bCadastroFornecedores){
                tipoTabelaNum = 3;
                complementoTabela = "(FORNECEDOR VARCHAR(30), CNPJ VARCHAR(30), TELEFONE BIGINT, ENDERECO VARCHAR(80))";
                tipoTabela.setText("Nova tabela de FORNECEDORES:");
                tipoTabela.setVisible(true);
                nomeLabel.setVisible(true);
                entradaNome.setVisible(true);
                bCriar.setVisible(true);
                revalidate();
            }
            else if (e.getSource() == bCriar) {
                try {
                    nomeTabela = entradaNome.getText().toUpperCase();
                    entradaNome.setText(null);

                    if (nomeTabela == null || nomeTabela.trim().isEmpty()){
                        JOptionPane.showMessageDialog(janelaPrincipal, "Erro ao criar a tabela.\nNome nao permitido.\n", "Erro", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    switch (tipoTabelaNum){
                    case 1:
                        nomeArquivoTabela = "CLI_"+nomeTabela;
                        break;
                    case 2:
                        nomeArquivoTabela = "PRO_"+nomeTabela;
                        break;
                    case 3:
                        nomeArquivoTabela = "FOR_"+nomeTabela;
                        break;
                    }
                    stmt = conexao.createStatement();
                    stmt.executeUpdate("CREATE TABLE " + nomeArquivoTabela + complementoTabela);
                    JOptionPane.showMessageDialog(janelaPrincipal, "Tabela " + nomeArquivoTabela + " criada com sucesso.", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                    tipoTabela.setVisible(false);
                    nomeLabel.setVisible(false);
                    entradaNome.setVisible(false);
                    bCriar.setVisible(false);
                    tipoTabelaNum = -1;
                    revalidate();
                } catch (SQLException ex) {
                    if (ex.getMessage().startsWith("Unexpected token")){
                        JOptionPane.showMessageDialog(janelaPrincipal, "Erro ao criar a tabela.\n Causa: Caracteres nao permitidos (espacos, / , < , * e afins)\n", "Erro", JOptionPane.ERROR_MESSAGE);
                    } else if (ex.getMessage().startsWith("Table already")){
                        JOptionPane.showMessageDialog(janelaPrincipal, "Erro ao criar a tabela.\n Causa: Tabela ja existente.\n", "Erro", JOptionPane.ERROR_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(janelaPrincipal, "Erro indefinido ao criar a tabela.\n"+ex, "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                    } catch (NullPointerException ex) {
                    JOptionPane.showMessageDialog(janelaPrincipal, "Problema interno.\n"+ex, "Erro", JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex){
                    JOptionPane.showMessageDialog(janelaPrincipal, "Problema indefinido.\n", "Erro", JOptionPane.ERROR_MESSAGE);
                 }
            }
        }
    }

    class JanelaSelecionarTabela extends JInternalFrame implements ActionListener {

        JLabel titulo = new JLabel("Selecione a tabela que deseja utilizar:");
        JButton bSelecionar = new JButton("Selecionar");
        JComboBox<Object> selecao;

        public JanelaSelecionarTabela(){
            super("Selecionar Tabela", false, true, false, true);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setLayout(new FlowLayout(FlowLayout.LEFT));
            bSelecionar.addActionListener(this);
            bSelecionar.setBackground(new Color(255,255,100));
            setBackground(new Color(255,255,220));
            getRootPane().setDefaultButton(bSelecionar);

            isSelecionarOpen = true;

            addInternalFrameListener(new InternalFrameAdapter(){ //Listener para saber quando as janelas de tabela são fechadas
                @Override
                public void	internalFrameClosed(InternalFrameEvent e){
                    isSelecionarOpen = false;
                }
            });

            try {
                atualizaOpcoes();
                if(opcoes.isEmpty()){
                    JOptionPane.showMessageDialog(janelaPrincipal, "Nenhuma tabela encontrada.\n", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                selecao = new JComboBox<>(opcoes.toArray());
                add(titulo);
                add(selecao);
                add(bSelecionar);
                setVisible(true);
            } catch (NullPointerException np){
                JOptionPane.showMessageDialog(janelaPrincipal, "Nenhuma tabela encontrada (null pointer).\n", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            } catch (Exception e){
                JOptionPane.showMessageDialog(janelaPrincipal, "Erro desconhecido.\n", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            pack();
        }

        public void actionPerformed(ActionEvent e) {
            if(e.getSource() == bSelecionar){

                if (!listaTabelasAbertas.isEmpty()){
                    for (int i = 0; i < listaTabelasAbertas.size(); i++){
                        if (selecao.getSelectedItem().toString().equals(listaTabelasAbertas.get(i).nome)){
                            JOptionPane.showMessageDialog(janelaPrincipal, "A lista " + selecao.getSelectedItem().toString() + " ja esta aberta.\n", "Erro", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                    for (int i = 0; i < listaTabelasAbertas.size(); i++){
                            try{
                                listaTabelasAbertas.get(i).setIcon(true);
                            } catch (Exception ex){
                                System.out.println("Erro: \n"+ex);
                            }

                    }

                }
                listaTabelasAbertas.add(new JanelaManipularTabela(selecao.getSelectedItem().toString()));
                this.dispose();
            }
        }
    }

    class JanelaDeletarTabela extends JInternalFrame implements ActionListener {
        JLabel titulo = new JLabel("Selecione a tabela que deseja deletar:");
        JButton bDeletar = new JButton("Deletar");
        String tabelaSelecionada;
        JComboBox<Object> selecao;

        public JanelaDeletarTabela(){
            super("Deletar Tabela", false, true, false, true);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setLayout(new FlowLayout(FlowLayout.LEFT));
            setBackground(new Color(255, 230, 230));
            bDeletar.addActionListener(this);
            bDeletar.setBackground(new Color(255,100,100));
            getRootPane().setDefaultButton(bDeletar);

            isDeletarOpen = true;

            addInternalFrameListener(new InternalFrameAdapter(){ //Listener para saber quando as janelas de tabela são fechadas
                @Override
                public void	internalFrameClosed(InternalFrameEvent e){
                    isDeletarOpen = false;
                }
            });

            try {
                atualizaOpcoes();
                if(opcoes.isEmpty()){
                    JOptionPane.showMessageDialog(janelaPrincipal, "Nenhuma tabela encontrada.\n", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                selecao = new JComboBox<>(opcoes.toArray());
                add(titulo);
                add(selecao);
                add(bDeletar);
                setVisible(true);
            } catch (NullPointerException np){
                JOptionPane.showMessageDialog(janelaPrincipal, "Nenhuma tabela encontrada (null pointer).\n", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            } catch (Exception e){
                JOptionPane.showMessageDialog(janelaPrincipal, "Erro desconhecido.\n", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            pack();
        }

        public void actionPerformed(ActionEvent e) {
            if(e.getSource() == bDeletar) {
                try {
                    tabelaSelecionada = selecao.getSelectedItem().toString();
                    stmt = conexao.createStatement();
                    stmt.executeUpdate("DROP TABLE " + tabelaSelecionada);
                    JOptionPane.showMessageDialog(janelaPrincipal, "A tabela " + tabelaSelecionada + " foi deletada.\n", "Tabela selecionada", JOptionPane.INFORMATION_MESSAGE);
                    this.dispose();
                    atualizaOpcoes();

                    if (!opcoes.isEmpty()){
                        janela_deletar = new JanelaDeletarTabela();
                        janelaPrincipal.add(janela_deletar);
                    }

                }catch (SQLException ex) {
                    JOptionPane.showMessageDialog(janelaPrincipal, "Erro ao deletar a tabela (SQL exception).\n", "Erro", JOptionPane.ERROR_MESSAGE);
                } catch (NullPointerException ex) {
                    JOptionPane.showMessageDialog(janelaPrincipal, "Problema interno (null pointer).\n", "Erro", JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex){
                    JOptionPane.showMessageDialog(janelaPrincipal, "Problema indefinido.\n", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

    }

    class JanelaManipularTabela extends JInternalFrame implements ActionListener {
        public String nome;
        Object[][] data;
        String[] columnNames;
        JTable tab;
        JScrollPane scroll;

        JButton bInserir = new JButton("Inserir");
        JButton bRemover = new JButton("Remover");
        JButton bProcurar = new JButton("Procurar");

        JPanel pBotoes = new JPanel(new GridLayout(3,1,2,2));
        JPanel pNome = new JPanel(new BorderLayout());
        JPanel cabecalhoNome = new JPanel(new FlowLayout(FlowLayout.LEFT));

        int rowCount=0;

        boolean isInserirOpen;
        boolean isRemoveOpen;
        boolean isProcuraOpen;

        private Insere insereFilho;
        private Remove removeFilho;
        private Procura procuraFilho;

        public JanelaManipularTabela(String nomeDaTabela){
            super(nomeDaTabela, true, true, true, true);
            isInserirOpen = false;
            isRemoveOpen = false;
            isProcuraOpen = false;
            tabelasAbertas++;
            JLabel labelNome = new JLabel(nomeDaTabela,JLabel.CENTER);
            nome = nomeDaTabela;
            setLocation(400,0);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            addInternalFrameListener(new InternalFrameAdapter(){ //Listener para saber quando as janelas de tabela são fechadas
                public void	internalFrameClosed(InternalFrameEvent e){
                    tabelasAbertas--;
                    for (int i = 0; i < listaTabelasAbertas.size(); i++){
                        if (nomeDaTabela.equals(listaTabelasAbertas.get(i).nome)){
                            listaTabelasAbertas.remove(i);
                            break;
                        }
                    }
                    if (isInserirOpen)
                    insereFilho.dispose();
                    if (isRemoveOpen)
                    removeFilho.dispose();
                    if (isProcuraOpen)
                    procuraFilho.dispose();
                }
            });

            if(isDeletarOpen){
                janela_deletar.dispose();
                isDeletarOpen = false;
            }

            bInserir.addActionListener(this);
            bRemover.addActionListener(this);
            bProcurar.addActionListener(this);

            setLayout(new BorderLayout(5,5));
            setMinimumSize(new Dimension(610,300));
            setPreferredSize(new Dimension(700,660));

            pBotoes.add(bInserir);
            pBotoes.add(bRemover);
            pBotoes.add(bProcurar);
            pBotoes.setPreferredSize(new Dimension(pBotoes.getPreferredSize().width,100));

            labelNome.setFont(new Font("Monospaced", Font.BOLD, 20));
            labelNome.setForeground(new Color(72, 111, 252));

            pNome.setPreferredSize(new Dimension(500,100));
            pNome.setBackground(new Color(220,220,220));
            pNome.add(new JLabel("Tabela: ", JLabel.CENTER),BorderLayout.NORTH);
            pNome.add(labelNome,BorderLayout.CENTER);

            cabecalhoNome.add(pNome);
            cabecalhoNome.add(pBotoes);
            add(cabecalhoNome,BorderLayout.NORTH);
                // DIFERENCIAR A EXIBICAO DOS 3 TIPOS DE TABELA
            if (nomeDaTabela.startsWith("CLI")){
                try{
                    columnNames = new String[5];
                    columnNames[0] =  "NOME"; columnNames[1] =  "CPF"; columnNames[2] =  "RG";
                    columnNames[3] =  "TELEFONE"; columnNames[4] =  "ENDERECO";
                    DatabaseMetaData md = conexao.getMetaData();
                    ResultSet rs = stmt.executeQuery("SELECT * FROM " + nomeDaTabela);
                    while (rs.next()) {
                        rowCount++;
                    }
                    rs = stmt.executeQuery("SELECT * FROM " + nomeDaTabela + " ORDER BY NOME");
                    data = new Object[rowCount][5];
                    while (rs.next()) {
                        data[rs.getRow()-1][0] = rs.getString("NOME");
                        data[rs.getRow()-1][1] = rs.getLong("CPF");
                        data[rs.getRow()-1][2] = rs.getLong("RG");
                        data[rs.getRow()-1][3] = rs.getLong("TELEFONE");
                        data[rs.getRow()-1][4] = rs.getString("ENDERECO");
                    }
                    tab = new JTable(new modeloTabela());
                    scroll = new JScrollPane(tab);
                    add(scroll,BorderLayout.CENTER);
                } catch (Exception e){
                    System.out.println("ERRO: \n"+e);
                }

            }
            else  if (nomeDaTabela.startsWith("PRO")){
                try{
                    columnNames = new String[5];
                    columnNames[0] =  "PRODUTO"; columnNames[1] =  "ID"; columnNames[2] =  "PRECO";
                    columnNames[3] =  "QUANTIDADE"; columnNames[4] =  "DEPARTAMENTO";
                    DatabaseMetaData md = conexao.getMetaData();
                    ResultSet rs = stmt.executeQuery("SELECT * FROM " + nomeDaTabela);
                    while (rs.next()) {
                        rowCount++;
                    }
                    rs = stmt.executeQuery("SELECT * FROM " + nomeDaTabela + " ORDER BY PRODUTO");
                    data = new Object[rowCount][5];
                    while (rs.next()) {
                        data[rs.getRow()-1][0] = rs.getString("PRODUTO");
                        data[rs.getRow()-1][1] = rs.getLong("ID");
                        data[rs.getRow()-1][2] = rs.getFloat("PRECO");
                        data[rs.getRow()-1][3] = rs.getInt("QUANTIDADE");
                        data[rs.getRow()-1][4] = rs.getString("DEPARTAMENTO");
                    }
                    tab = new JTable(new modeloTabela());
                    scroll = new JScrollPane(tab);
                    add(scroll,BorderLayout.CENTER);
                } catch (Exception e){
                    System.out.println("ERRO: \n"+e);
                }
            }
            else if (nomeDaTabela.startsWith("FOR")){
                try{
                    columnNames = new String[4];
                    columnNames[0] =  "FORNECEDOR"; columnNames[1] =  "CNPJ";
                    columnNames[2] =  "TELEFONE"; columnNames[3] =  "ENDERECO";
                    DatabaseMetaData md = conexao.getMetaData();
                    ResultSet rs = stmt.executeQuery("SELECT * FROM " + nomeDaTabela);
                    while (rs.next()) {
                        rowCount++;
                    }
                    rs = stmt.executeQuery("SELECT * FROM " + nomeDaTabela + " ORDER BY FORNECEDOR");
                    data = new Object[rowCount][4];
                    while (rs.next()) {
                        data[rs.getRow()-1][0] = rs.getString("FORNECEDOR");
                        data[rs.getRow()-1][1] = rs.getString("CNPJ");
                        data[rs.getRow()-1][2] = rs.getLong("TELEFONE");
                        data[rs.getRow()-1][3] = rs.getString("ENDERECO");
                    }
                    tab = new JTable(new modeloTabela());
                    scroll = new JScrollPane(tab);
                    add(scroll,BorderLayout.CENTER);
                } catch (Exception e){
                    System.out.println("ERRO: \n"+e);
                }
            }
            pack();
            setVisible(true);
            janelaPrincipal.add(this);
            this.toFront();
        }

        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == bInserir){
                if (this.isInserirOpen == false) {
                    insereFilho = new Insere(this.nome, this);
                } else {
                    JOptionPane.showMessageDialog(janelaPrincipal, "Ja existe uma janela para inserir em " + this.nome + " aberta\n", "Aviso", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            } else if (e.getSource() == bRemover){
                if (this.isRemoveOpen == false) {
                    removeFilho = new Remove(this.nome, this);
                } else {
                    JOptionPane.showMessageDialog(janelaPrincipal, "Ja existe uma janela para remover em " + this.nome + " aberta.\n", "Aviso", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            } else if (e.getSource() == bProcurar){
                if (this.isProcuraOpen == false){
                    procuraFilho = new Procura(this.nome, this);
                } else {
                    JOptionPane.showMessageDialog(janelaPrincipal, "Ja existe uma janela para procurar em " + this.nome + " aberta.\n", "Aviso", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
        }
        class modeloTabela extends AbstractTableModel {

            public int getColumnCount() {
                return columnNames.length;
            }

            public int getRowCount() {
                return data.length;
            }

            public String getColumnName(int col) {
                return columnNames[col];
            }

            public Object getValueAt(int row, int col) {
                return data[row][col];
            }

            public boolean isCellEditable(int row, int col) {
                return false;
            }
        }
    }

    class Insere extends JInternalFrame implements ActionListener{
        PreparedStatement pStmt;
        String nomeInterno;

        JPanel pEntrada = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();

        JTextField campo1 = new JTextField(30);
        JTextField campo2 = new JTextField(30);
        JTextField campo3 = new JTextField(30);
        JTextField campo4 = new JTextField(30);
        JTextField campo5 = new JTextField(30);

        JButton bInserir = new JButton("Inserir");

        public Insere(String nomeTabela, JanelaManipularTabela objetoChamada) {
            super("Inserir",false,true,false,false);
            objetoChamada.isInserirOpen = true;
            nomeInterno = nomeTabela;
            bInserir.addActionListener(this);
            getRootPane().setDefaultButton(bInserir);
            
            addInternalFrameListener(new InternalFrameAdapter(){ //Listener para saber quando as janelas de tabela são fechadas
                public void	internalFrameClosed(InternalFrameEvent e){
                    objetoChamada.isInserirOpen = false;
                    for (int i = 0; i < listaTabelasAbertas.size(); i++){
                        if (nomeInterno.equals(listaTabelasAbertas.get(i).nome)){
                            objetoChamada.dispose();
                            listaTabelasAbertas.add(new JanelaManipularTabela(nomeTabela));
                            break;
                        }
                    }
                }
            });

            gc.fill = GridBagConstraints.HORIZONTAL;
            gc.anchor = GridBagConstraints.NORTHWEST;

            try {

                if (nomeTabela.startsWith("CLI")) { ///////////////////////////////// CLIENTES

                    gc.gridy = 0; gc.gridx = 0; gc.weightx = 0.0; gc.gridwidth = 1;
                    pEntrada.add(new JLabel("NOME: ", JLabel.CENTER),gc);
                    gc.gridx = 1; gc.weightx = 1.0; gc.gridwidth = gc.REMAINDER;
                    pEntrada.add(campo1,gc);
                    gc.gridy = 1; gc.gridx = 0; gc.weightx = 0.0; gc.gridwidth = 1;
                    pEntrada.add(new JLabel("CPF: ", JLabel.CENTER),gc);
                    gc.gridx = 1; gc.weightx = 1.0; gc.gridwidth = gc.REMAINDER;
                    pEntrada.add(campo2,gc);
                    gc.gridy = 2; gc.gridx = 0; gc.weightx = 0.0; gc.gridwidth = 1;
                    pEntrada.add(new JLabel("RG: ", JLabel.CENTER),gc);
                    gc.gridx = 1; gc.weightx = 1.0; gc.gridwidth = gc.REMAINDER;
                    pEntrada.add(campo3,gc);
                    gc.gridy = 3; gc.gridx = 0; gc.weightx = 0.0; gc.gridwidth = 1;
                    pEntrada.add(new JLabel("TELEFONE: ", JLabel.CENTER),gc);
                    gc.gridx = 1; gc.weightx = 1.0; gc.gridwidth = gc.REMAINDER;
                    pEntrada.add(campo4,gc);
                    gc.gridy = 4; gc.gridx = 0; gc.weightx = 0.0; gc.gridwidth = 1;
                    pEntrada.add(new JLabel("ENDERECO: ", JLabel.CENTER),gc);
                    gc.gridx = 1; gc.weightx = 1.0; gc.gridwidth = gc.REMAINDER;
                    pEntrada.add(campo5,gc);

                    gc.gridy = 5; gc.gridx = 1; gc.weightx = 0.0; gc.gridwidth = gc.REMAINDER;
                    pEntrada.add(new JSeparator());

                    gc.gridy = 6; gc.gridx = 1; gc.weightx = 1; gc.gridwidth = 1;
                    pEntrada.add(new JLabel(), gc);
                    gc.gridy = 6; gc.gridx = 2; gc.weightx = 0.0; gc.gridwidth = 1;
                    pEntrada.add(bInserir, gc);
                    gc.gridy = 6; gc.gridx = 3; gc.weightx = 1; gc.gridwidth = gc.REMAINDER;
                    pEntrada.add(new JPanel(), gc);

                    add(pEntrada,BorderLayout.CENTER);
                } else if (nomeTabela.startsWith("PRO")) { ///////////////////////////////// PRODUTOS

                    gc.gridy = 0; gc.gridx = 0; gc.weightx = 0.0; gc.gridwidth = 1;
                    pEntrada.add(new JLabel("PRODUTO: ", JLabel.CENTER),gc);
                    gc.gridx = 1; gc.weightx = 1.0; gc.gridwidth = gc.REMAINDER;
                    pEntrada.add(campo1,gc);
                    gc.gridy = 1; gc.gridx = 0; gc.weightx = 0.0; gc.gridwidth = 1;
                    pEntrada.add(new JLabel("ID: ", JLabel.CENTER),gc);
                    gc.gridx = 1; gc.weightx = 1.0; gc.gridwidth = gc.REMAINDER;
                    pEntrada.add(campo2,gc);
                    gc.gridy = 2; gc.gridx = 0; gc.weightx = 0.0; gc.gridwidth = 1;
                    pEntrada.add(new JLabel("PRECO: ", JLabel.CENTER),gc);
                    gc.gridx = 1; gc.weightx = 1.0; gc.gridwidth = gc.REMAINDER;
                    pEntrada.add(campo3,gc);
                    gc.gridy = 3; gc.gridx = 0; gc.weightx = 0.0; gc.gridwidth = 1;
                    pEntrada.add(new JLabel("QUANTIDADE: ", JLabel.CENTER),gc);
                    gc.gridx = 1; gc.weightx = 1.0; gc.gridwidth = gc.REMAINDER;
                    pEntrada.add(campo4,gc);
                    gc.gridy = 4; gc.gridx = 0; gc.weightx = 0.0; gc.gridwidth = 1;
                    pEntrada.add(new JLabel("DEPARTAMENTO: ", JLabel.CENTER),gc);
                    gc.gridx = 1; gc.weightx = 1.0; gc.gridwidth = gc.REMAINDER;
                    pEntrada.add(campo5,gc);

                    gc.gridy = 5; gc.gridx = 1; gc.weightx = 0.0; gc.gridwidth = gc.REMAINDER;
                    pEntrada.add(new JSeparator());

                    gc.gridy = 6; gc.gridx = 1; gc.weightx = 1; gc.gridwidth = 1;
                    pEntrada.add(new JLabel(), gc);
                    gc.gridy = 6; gc.gridx = 2; gc.weightx = 0.0; gc.gridwidth = 1;
                    pEntrada.add(bInserir, gc);
                    gc.gridy = 6; gc.gridx = 3; gc.weightx = 1; gc.gridwidth = gc.REMAINDER;
                    pEntrada.add(new JPanel(), gc);

                    add(pEntrada,BorderLayout.CENTER);
                } else if (nomeTabela.startsWith("FOR")) { ///////////////////////////////// FORNECEDORES
                    gc.gridy = 0; gc.gridx = 0; gc.weightx = 0.0; gc.gridwidth = 1;
                    pEntrada.add(new JLabel("FORNECEDOR: ", JLabel.CENTER),gc);
                    gc.gridx = 1; gc.weightx = 1.0; gc.gridwidth = gc.REMAINDER;
                    pEntrada.add(campo1,gc);
                    gc.gridy = 1; gc.gridx = 0; gc.weightx = 0.0; gc.gridwidth = 1;
                    pEntrada.add(new JLabel("CNPJ: ", JLabel.CENTER),gc);
                    gc.gridx = 1; gc.weightx = 1.0; gc.gridwidth = gc.REMAINDER;
                    pEntrada.add(campo2,gc);
                    gc.gridy = 2; gc.gridx = 0; gc.weightx = 0.0; gc.gridwidth = 1;
                    pEntrada.add(new JLabel("TELEFONE: ", JLabel.CENTER),gc);
                    gc.gridx = 1; gc.weightx = 1.0; gc.gridwidth = gc.REMAINDER;
                    pEntrada.add(campo3,gc);
                    gc.gridy = 3; gc.gridx = 0; gc.weightx = 0.0; gc.gridwidth = 1;
                    pEntrada.add(new JLabel("ENDERECO: ", JLabel.CENTER),gc);
                    gc.gridx = 1; gc.weightx = 1.0; gc.gridwidth = gc.REMAINDER;
                    pEntrada.add(campo4,gc);

                    gc.gridy = 4; gc.gridx = 1; gc.weightx = 0.0; gc.gridwidth = gc.REMAINDER;
                    pEntrada.add(new JSeparator());

                    gc.gridy = 5; gc.gridx = 1; gc.weightx = 1; gc.gridwidth = 1;
                    pEntrada.add(new JLabel(), gc);
                    gc.gridy = 5; gc.gridx = 2; gc.weightx = 0.0; gc.gridwidth = 1;
                    pEntrada.add(bInserir, gc);
                    gc.gridy = 5; gc.gridx = 3; gc.weightx = 1; gc.gridwidth = gc.REMAINDER;
                    pEntrada.add(new JPanel(), gc);

                    add(pEntrada,BorderLayout.CENTER);
                }
            } catch (Exception e){
                System.out.println("Erro!! \n"+e);
            }
            setLocation(380,200);
            setVisible(true);
            pack();
            janelaPrincipal.add(this);
            this.toFront();
            campo1.requestFocus();
        }

        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == bInserir){
                try {
                    if (nomeInterno.startsWith("CLI")){ ///////////////////////////////// CLIENTES
                        pStmt = conexao.prepareStatement("INSERT INTO " + nomeInterno + " VALUES (?, ?, ?, ?, ?)");
                        pStmt.setString(1, campo1.getText().toUpperCase());
                        pStmt.setLong(2, Long.parseLong(campo2.getText()));
                        pStmt.setLong(3, Long.parseLong(campo3.getText()));
                        pStmt.setLong(4, Long.parseLong(campo4.getText()));
                        pStmt.setString(5, campo5.getText().toUpperCase());
                        pStmt.executeUpdate();
                        this.dispose();
                    } else if (nomeInterno.startsWith("PRO")){ ///////////////////////////////// PRODUTOS
                        pStmt = conexao.prepareStatement("INSERT INTO " + nomeInterno + " VALUES (?, ?, ?, ?, ?)");
                        pStmt.setString(1, campo1.getText().toUpperCase());
                        pStmt.setLong(2, Long.parseLong(campo2.getText()));
                        pStmt.setFloat(3, Float.parseFloat(campo3.getText()));
                        pStmt.setInt(4, Integer.parseInt(campo4.getText()));
                        pStmt.setString(5, campo5.getText().toUpperCase());
                        pStmt.executeUpdate();
                        this.dispose();
                    } else if (nomeInterno.startsWith("FOR")){ ///////////////////////////////// FORNECEDORES
                        pStmt = conexao.prepareStatement("INSERT INTO " + nomeInterno + " VALUES (?, ?, ?, ?)");
                        pStmt.setString(1, campo1.getText().toUpperCase());
                        pStmt.setString(2, campo2.getText().toUpperCase());
                        pStmt.setLong(3, Long.parseLong(campo3.getText()));
                        pStmt.setString(4, campo4.getText().toUpperCase());
                        pStmt.executeUpdate();
                        this.dispose();
                    }

                } catch (NumberFormatException ne){
                    JOptionPane.showMessageDialog(janelaPrincipal, "Formato de numeros inadequado.", "Erro", JOptionPane.ERROR_MESSAGE);
                }
                catch (Exception ex){
                    System.out.println("Erro!\n"+ex);
                }
            }
        }
    }

    class Remove extends JInternalFrame implements ActionListener {
        String [] colunas;
        JComboBox<String> comboColunas;
        JButton bRemover = new JButton("Remover");
        JTextField valorRemover = new JTextField(10);
        PreparedStatement pStmt;
        String nomeInterno;

        public Remove (String nomeTabela, JanelaManipularTabela objetoChamada){
            super("Remover Dados",false,true,false,false);
            objetoChamada.isRemoveOpen = true;
            setLayout(new FlowLayout());
            setLocation(380,200);
            bRemover.addActionListener(this);
            nomeInterno = nomeTabela;
            getRootPane().setDefaultButton(bRemover);

            try{
                addInternalFrameListener(new InternalFrameAdapter(){ //Listener para saber quando as janelas de tabela são fechadas
                    public void	internalFrameClosed(InternalFrameEvent e){
                        objetoChamada.isRemoveOpen = false;
                        for (int i = 0; i < listaTabelasAbertas.size(); i++){
                            if (nomeTabela.equals(listaTabelasAbertas.get(i).nome)){
                                objetoChamada.dispose();
                                listaTabelasAbertas.add(new JanelaManipularTabela(nomeTabela));
                                break;
                            }
                        }
                    }
                });

                if (nomeTabela.startsWith("CLI")){ ///////////////////////////////// CLIENTES
                    colunas = new String[5];
                    colunas[0] = "NOME"; colunas[1] = "CPF"; colunas[2] = "RG";
                    colunas[3] = "TELEFONE"; colunas[4] = "ENDERECO";

                } else if (nomeTabela.startsWith("PRO")){ ///////////////////////////////// PRODUTOS
                    colunas = new String[5];
                    colunas[0] = "PRODUTO"; colunas[1] = "ID"; colunas[2] = "PRECO";
                    colunas[3] = "QUANTIDADE"; colunas[4] = "DEPARTAMENTO";

                } else { ///////////////////////////////// FORNECEDORES
                    colunas = new String[4];
                    colunas[0] = "FORNECEDOR"; colunas[1] = "CNPJ"; colunas[2] = "TELEFONE";
                    colunas[3] = "ENDERECO";
                }
                comboColunas = new JComboBox<>(colunas);
                add(new JLabel("Escolha por qual coluna deseja remover: "));
                add(comboColunas);
                add(new JLabel("Valor: "));
                add(valorRemover);
                add(bRemover);
                setVisible(true);
                pack();
                janelaPrincipal.add(this);
                this.toFront();
                valorRemover.requestFocus();
            } catch (Exception ex){
                System.out.println("Erro!\n"+ex);
            }
        }

        public void	actionPerformed(ActionEvent e){
            if(e.getSource() == bRemover){
                try {
                    if(nomeInterno.startsWith("CLI")) { ///////////////////////////////// CLIENTES
                        switch (comboColunas.getSelectedItem().toString()) {
                            case ("NOME"):
                                pStmt = conexao.prepareStatement("DELETE FROM " + nomeInterno + " WHERE NOME=?");
                                pStmt.setString(1, valorRemover.getText().toUpperCase());
                                break;
                            case ("CPF"):
                                pStmt = conexao.prepareStatement("DELETE FROM " + nomeInterno + " WHERE CPF=?");
                                pStmt.setLong(1, Long.parseLong(valorRemover.getText()));
                                break;
                            case ("RG"):
                                pStmt = conexao.prepareStatement("DELETE FROM " + nomeInterno + " WHERE RG=?");
                                pStmt.setLong(1, Long.parseLong(valorRemover.getText()));
                                break;
                            case ("TELEFONE"):
                                pStmt = conexao.prepareStatement("DELETE FROM " + nomeInterno + " WHERE TELEFONE=?");
                                pStmt.setLong(1, Long.parseLong(valorRemover.getText()));
                                break;
                            case ("ENDERECO"):
                                pStmt = conexao.prepareStatement("DELETE FROM " + nomeInterno + " WHERE ENDERECO=?");
                                pStmt.setString(1, valorRemover.getText().toUpperCase());
                                break;
                        }
                        pStmt.executeUpdate();
                        JOptionPane.showMessageDialog(janelaPrincipal, "O(s) cadastro(s) de  " + comboColunas.getSelectedItem().toString() + " = " + valorRemover.getText().toUpperCase() + "  foi/foram removido(s).\n", "Removido", JOptionPane.INFORMATION_MESSAGE);
                        this.dispose();
                    } else if (nomeInterno.startsWith("PRO")){ ///////////////////////////////// PRODUTOS
                        switch (comboColunas.getSelectedItem().toString()) {
                            case ("PRODUTO"):
                                pStmt = conexao.prepareStatement("DELETE FROM " + nomeInterno + " WHERE PRODUTO=?");
                                pStmt.setString(1, valorRemover.getText().toUpperCase());
                                break;
                            case ("ID"):
                                pStmt = conexao.prepareStatement("DELETE FROM " + nomeInterno + " WHERE ID=?");
                                pStmt.setLong(1, Long.parseLong(valorRemover.getText()));
                                break;
                            case ("PRECO"):
                                pStmt = conexao.prepareStatement("DELETE FROM " + nomeInterno + " WHERE PRECO=?");
                                pStmt.setFloat(1, Float.parseFloat(valorRemover.getText()));
                                break;
                            case ("QUANTIDADE"):
                                pStmt = conexao.prepareStatement("DELETE FROM " + nomeInterno + " WHERE QUANTIDADE=?");
                                pStmt.setInt(1, Integer.parseInt(valorRemover.getText()));
                                break;
                            case ("DEPARTAMENTO"):
                                pStmt = conexao.prepareStatement("DELETE FROM " + nomeInterno + " WHERE DEPARTAMENTO=?");
                                pStmt.setString(1, valorRemover.getText().toUpperCase());
                                break;
                        }
                        pStmt.executeUpdate();
                        JOptionPane.showMessageDialog(janelaPrincipal, "O cadastro de  " + comboColunas.getSelectedItem().toString() + " = " + valorRemover.getText().toUpperCase() + "  foi removido.\n", "Removido", JOptionPane.INFORMATION_MESSAGE);
                        this.dispose();
                    } else if (nomeInterno.startsWith("FOR")){ ///////////////////////////////// FORNECEDORES
                        switch (comboColunas.getSelectedItem().toString()) {
                            case ("FORNECEDOR"):
                                pStmt = conexao.prepareStatement("DELETE FROM " + nomeInterno + " WHERE FORNECEDOR=?");
                                pStmt.setString(1, valorRemover.getText().toUpperCase());
                                break;
                            case ("CNPJ"):
                                pStmt = conexao.prepareStatement("DELETE FROM " + nomeInterno + " WHERE CNPJ=?");
                                pStmt.setString(1, valorRemover.getText().toUpperCase());
                                break;
                            case ("TELEFONE"):
                                pStmt = conexao.prepareStatement("DELETE FROM " + nomeInterno + " WHERE TELEFONE=?");
                                pStmt.setLong(1, Long.parseLong(valorRemover.getText()));
                                break;
                            case ("ENDERECO"):
                                pStmt = conexao.prepareStatement("DELETE FROM " + nomeInterno + " WHERE ENDERECO=?");
                                pStmt.setString(1, valorRemover.getText().toUpperCase());
                                break;
                        }
                        pStmt.executeUpdate();
                        JOptionPane.showMessageDialog(janelaPrincipal, "O cadastro de  " + comboColunas.getSelectedItem().toString() + " = " + valorRemover.getText().toUpperCase() + "  foi removido.\n", "Removido", JOptionPane.INFORMATION_MESSAGE);
                        this.dispose();
                    }
                } catch (SQLException sqle){
                    JOptionPane.showMessageDialog(janelaPrincipal, "Erro na remoção.", "Erro", JOptionPane.ERROR_MESSAGE);
                } catch (Exception exc){
                    System.out.println("Erro!\n"+exc);
                }
            }
        }
    }

    class Procura extends JInternalFrame implements ActionListener {
        Object[][] data;
        String[] columnNames;
        String [] colunas;
        JTable tab;
        JScrollPane scroll;
        JComboBox<String> comboColunas;
        JPanel pNorte = new JPanel(new FlowLayout());
        JPanel pBotoes = new JPanel(new GridLayout(2,1,3,3));
        JButton bProcura = new JButton("Procurar");
        JButton bFechar = new JButton("Fechar");
        JTextField valorProcurar = new JTextField(10);
        PreparedStatement pStmt;
        String nomeInterno;
        int rowCount;
        boolean recycle = false;
        public Procura (String nomeTabela, JanelaManipularTabela objetoChamada){
            super("Procurar Dados",true,true,true,false);
            objetoChamada.isProcuraOpen = true;
            setLayout(new BorderLayout());
            setLocation(380,200);
            bProcura.addActionListener(this);
            bFechar.addActionListener(this);
            nomeInterno = nomeTabela;
            pBotoes.add(bProcura);
            pBotoes.add(bFechar);
            getRootPane().setDefaultButton(bProcura);

            try{
                addInternalFrameListener(new InternalFrameAdapter(){ //Listener para saber quando as janelas de tabela são fechadas
                    public void	internalFrameClosed(InternalFrameEvent e){
                        objetoChamada.isProcuraOpen = false;
                    }
                });

                if (nomeTabela.startsWith("CLI")){
                    colunas = new String[5];
                    colunas[0] = "NOME"; colunas[1] = "CPF"; colunas[2] = "RG";
                    colunas[3] = "TELEFONE"; colunas[4] = "ENDERECO";

                } else if (nomeTabela.startsWith("PRO")){
                    colunas = new String[5];
                    colunas[0] = "PRODUTO"; colunas[1] = "ID"; colunas[2] = "PRECO";
                    colunas[3] = "QUANTIDADE"; colunas[4] = "DEPARTAMENTO";

                } else {
                    colunas = new String[4];
                    colunas[0] = "FORNECEDOR"; colunas[1] = "CNPJ"; colunas[2] = "TELEFONE";
                    colunas[3] = "ENDERECO";
                }
                comboColunas = new JComboBox<>(colunas);
                pNorte.add(new JLabel("Escolha por qual coluna deseja procurar: "));
                pNorte.add(comboColunas);
                pNorte.add(new JLabel("Valor: "));
                pNorte.add(valorProcurar);
                pNorte.add(pBotoes);
                add(pNorte,BorderLayout.NORTH);
                setVisible(true);
                setMinimumSize(new Dimension(600,100));
                pack();
                janelaPrincipal.add(this);
                this.toFront();
                valorProcurar.requestFocus();
            } catch (Exception ex){
                System.out.println("Erro!\n"+ex);
            }
        }

        public void	actionPerformed(ActionEvent e){
            if (e.getSource() == bFechar){
                this.dispose();
            } else if(e.getSource() == bProcura){
                try {
                    if (recycle == true){
                        remove(scroll);
                    }
                    rowCount = 0;
                    if (valorProcurar.getText().trim().isEmpty() || valorProcurar.getText() == null){
                        JOptionPane.showMessageDialog(janelaPrincipal, "Necessario completar o campo de busca.\n", "Erro", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if (nomeInterno.startsWith("CLI")) {    ///////////////////////////////// CLIENTES
                        switch (comboColunas.getSelectedItem().toString()) {
                            case ("NOME"):
                                pStmt = conexao.prepareStatement("SELECT * FROM " + nomeInterno + " WHERE NOME LIKE ? ORDER BY NOME");
                                pStmt.setString(1, valorProcurar.getText().toUpperCase().concat("%"));
                                break;
                            case ("CPF"):
                                pStmt = conexao.prepareStatement("SELECT * FROM " + nomeInterno + " WHERE CPF=? ORDER BY NOME");
                                pStmt.setLong(1, Long.parseLong(valorProcurar.getText()));
                                break;
                            case ("RG"):
                                pStmt = conexao.prepareStatement("SELECT * FROM " + nomeInterno + " WHERE RG=? ORDER BY NOME");
                                pStmt.setLong(1, Long.parseLong(valorProcurar.getText()));
                                break;
                            case ("TELEFONE"):
                                pStmt = conexao.prepareStatement("SELECT * FROM " + nomeInterno + " WHERE TELEFONE=? ORDER BY NOME");
                                pStmt.setLong(1, Long.parseLong(valorProcurar.getText()));
                                break;
                            case ("ENDERECO"):
                                pStmt = conexao.prepareStatement("SELECT * FROM " + nomeInterno + " WHERE ENDERECO LIKE ? ORDER BY NOME");
                                pStmt.setString(1, valorProcurar.getText().toUpperCase().concat("%"));
                                break;
                        }
                        ResultSet rs = pStmt.executeQuery();
                        while (rs.next()) {
                            rowCount++;
                        }
                        if (rowCount == 0){
                            JOptionPane.showMessageDialog(janelaPrincipal, "Nenhum dado encontrado com " + comboColunas.getSelectedItem().toString() + " = " + valorProcurar.getText().toUpperCase() + ".", "Aviso", JOptionPane.INFORMATION_MESSAGE);
                            this.dispose();
                        }
                        rs = pStmt.executeQuery();
                        data = new Object[rowCount][5];
                        while (rs.next()) {
                            data[rs.getRow() - 1][0] = rs.getString("NOME");
                            data[rs.getRow() - 1][1] = rs.getLong("CPF");
                            data[rs.getRow() - 1][2] = rs.getLong("RG");
                            data[rs.getRow() - 1][3] = rs.getLong("TELEFONE");
                            data[rs.getRow() - 1][4] = rs.getString("ENDERECO");
                        }
                        columnNames = new String[5];
                        columnNames[0] =  "NOME"; columnNames[1] =  "CPF"; columnNames[2] =  "RG";
                        columnNames[3] =  "TELEFONE"; columnNames[4] =  "ENDERECO";

                        tab = new JTable(new modeloTabela());
                        scroll = new JScrollPane(tab);
                        add(scroll, BorderLayout.CENTER);
                        recycle = true;
                        pack();
                    } else if (nomeInterno.startsWith("PRO")){  ///////////////////////////////// PRODUTOS
                        switch (comboColunas.getSelectedItem().toString()) {
                            case ("PRODUTO"):
                                pStmt = conexao.prepareStatement("SELECT * FROM " + nomeInterno + " WHERE PRODUTO LIKE ? ORDER BY PRODUTO");
                                pStmt.setString(1, valorProcurar.getText().toUpperCase().concat("%"));
                                break;
                            case ("ID"):
                                pStmt = conexao.prepareStatement("SELECT * FROM " + nomeInterno + " WHERE ID=? ORDER BY PRODUTO");
                                pStmt.setLong(1, Long.parseLong(valorProcurar.getText()));
                                break;
                            case ("PRECO"):
                                pStmt = conexao.prepareStatement("SELECT * FROM " + nomeInterno + " WHERE PRECO=? ORDER BY PRODUTO");
                                pStmt.setFloat(1, Float.parseFloat(valorProcurar.getText()));
                                break;
                            case ("QUANTIDADE"):
                                pStmt = conexao.prepareStatement("SELECT * FROM " + nomeInterno + " WHERE QUANTIDADE=? ORDER BY PRODUTO");
                                pStmt.setInt(1, Integer.parseInt(valorProcurar.getText()));
                                break;
                            case ("DEPARTAMENTO"):
                                pStmt = conexao.prepareStatement("SELECT * FROM " + nomeInterno + " WHERE DEPARTAMENTO LIKE ? ORDER BY PRODUTO");
                                pStmt.setString(1, valorProcurar.getText().toUpperCase().concat("%"));
                                break;
                        }
                        ResultSet rs = pStmt.executeQuery();
                        while (rs.next()) {
                            rowCount++;
                        }
                        if (rowCount == 0){
                            JOptionPane.showMessageDialog(janelaPrincipal, "Nenhum dado encontrado de " + comboColunas.getSelectedItem().toString() + " = " + valorProcurar.getText().toUpperCase() + " foi encontrado.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
                            this.dispose();
                        }
                        rs = pStmt.executeQuery();
                        data = new Object[rowCount][5];
                        while (rs.next()) {
                            data[rs.getRow() - 1][0] = rs.getString("PRODUTO");
                            data[rs.getRow() - 1][1] = rs.getLong("ID");
                            data[rs.getRow() - 1][2] = rs.getFloat("PRECO");
                            data[rs.getRow() - 1][3] = rs.getInt("QUANTIDADE");
                            data[rs.getRow() - 1][4] = rs.getString("DEPARTAMENTO");
                        }
                        columnNames = new String[5];
                        columnNames[0] =  "PRODUTO"; columnNames[1] =  "ID"; columnNames[2] =  "PRECO";
                        columnNames[3] =  "QUANTIDADE"; columnNames[4] =  "DEPARTAMENTO";
                        tab = new JTable(new modeloTabela());
                        scroll = new JScrollPane(tab);
                        add(scroll, BorderLayout.CENTER);
                        pack();
                    } else {  ///////////////////////////////// FORNECEDORES
                        switch (comboColunas.getSelectedItem().toString()) {
                            case ("FORNECEDOR"):
                                pStmt = conexao.prepareStatement("SELECT * FROM " + nomeInterno + " WHERE FORNECEDOR LIKE ? ORDER BY FORNECEDOR");
                                pStmt.setString(1, valorProcurar.getText().toUpperCase().concat("%"));
                                break;
                            case ("CNPJ"):
                                pStmt = conexao.prepareStatement("SELECT * FROM " + nomeInterno + " WHERE CNPJ=? ORDER BY FORNECEDOR");
                                pStmt.setString(1, valorProcurar.getText().toUpperCase());
                                break;
                            case ("TELEFONE"):
                                pStmt = conexao.prepareStatement("SELECT * FROM " + nomeInterno + " WHERE TELEFONE=? ORDER BY FORNECEDOR");
                                pStmt.setLong(1, Long.parseLong(valorProcurar.getText()));
                                break;
                            case ("ENDERECO"):
                                pStmt = conexao.prepareStatement("SELECT * FROM " + nomeInterno + " WHERE ENDERECO LIKE ? ORDER BY FORNECEDOR");
                                pStmt.setString(1, valorProcurar.getText().toUpperCase().concat("%"));
                                break;
                        }
                        ResultSet rs = pStmt.executeQuery();
                        while (rs.next()) {
                            rowCount++;
                        }
                        if (rowCount == 0){
                            JOptionPane.showMessageDialog(janelaPrincipal, "Nenhum dado encontrado de " + comboColunas.getSelectedItem().toString() + " = " + valorProcurar.getText().toUpperCase() + " foi encontrado.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
                            this.dispose();
                        }
                        rs = pStmt.executeQuery();
                        data = new Object[rowCount][4];
                        while (rs.next()) {
                            data[rs.getRow() - 1][0] = rs.getString("FORNECEDOR");
                            data[rs.getRow() - 1][1] = rs.getString("CNPJ");
                            data[rs.getRow() - 1][2] = rs.getLong("TELEFONE");
                            data[rs.getRow() - 1][3] = rs.getString("ENDERECO");
                        }
                        columnNames = new String[4];
                        columnNames[0] =  "FORNECEDOR"; columnNames[1] =  "CNPJ";
                        columnNames[2] =  "TELEFONE"; columnNames[3] =  "ENDERECO";
                        tab = new JTable(new modeloTabela());
                        scroll = new JScrollPane(tab);
                        add(scroll, BorderLayout.CENTER);
                        pack();
                    }
                } catch (SQLException sqle){
                    JOptionPane.showMessageDialog(janelaPrincipal, "Erro na busca.", "Erro", JOptionPane.ERROR_MESSAGE);
                    System.out.println("Erro!\n"+sqle);
                } catch (Exception exc){
                    System.out.println("Erro!\n"+exc);
                }
            }
        }

        class modeloTabela extends AbstractTableModel {

            public int getColumnCount() {
                return columnNames.length;
            }

            public int getRowCount() {
                return data.length;
            }

            public String getColumnName(int col) {
                return columnNames[col];
            }

            public Object getValueAt(int row, int col) {
                return data[row][col];
            }

            public boolean isCellEditable(int row, int col) {
                return false;
            }
        }
    }

    public static void main(String[] args) {
        new Programa();
    }
}
