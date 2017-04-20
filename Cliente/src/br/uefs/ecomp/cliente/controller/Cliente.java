package br.uefs.ecomp.cliente.controller;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.InputMismatchException;
import java.util.Scanner;


import br.uefs.ecomp.cliente.model.Acao;
/**
 * 
 * @author Victor Munduruca
 * Essa classe realiza a comunica��o entre o cliente e servidor, recebendo os dados, preparando-os e enviando.
 */

public class Cliente {

	private static boolean estaLogado; // V�riavel que indica se o cliente est� logado ou n�o
	private static String numeroContaLogado; // 
	public Cliente() {
		estaLogado = false;
	}
 	public static void main(String[] args) throws UnknownHostException, IOException, NoSuchAlgorithmException {
 		try{
 			new Cliente().executa(); //m�todo que inicializa as a��es do cliente
 		} catch (ConnectException e) {
 			System.out.println("Erro, n�o conseguiu conectar ao servidor");
 		} 
		return;
	}
 
 	/**
 	 * M�todo que realiza as a��es de cliente, por uma interface de menu. Onde os pacotes s�o preenchidos, enviados e poss�veis respostas do servidor informadas ao usu�rio
 	 * @throws IOException
 	 * @throws NoSuchAlgorithmException
 	 */
	public void executa() throws IOException, NoSuchAlgorithmException {
		
		Scanner scanner = new Scanner(System.in);
		
		
			while(true) { // La�o de repeti��o utilizado para caso o usu�rio deseje realizar mais de uma opera��o
				Socket cliente = new Socket("192.168.1.8", 12346); // Socket � conectado ao servidor, por um ip e porta especificados
				System.out.println("O cliente se conectou ao servidor!"); // Mensagem de conex�o � passada ao usu�rio
				DataOutputStream outputDados = new DataOutputStream(cliente.getOutputStream()); // Saida de dados, com base no socket do servidor
				DataInputStream inputDados = new DataInputStream(cliente.getInputStream()); // Entrada de dados com base no socket do servidor
				System.out.println("Escolha a op��o desejada\n1 - Cadastrar Conta\n2 - Fazer login\n3- Fazer transferencia\n4- Fazer deposito\n5- Cadastrar novo titular\n6-Encerrar sess�o"); //Menu com as escolhas mostradas ao usu�rio
				int acao = scanner.nextInt(); // A a��o desejada � obtida do usu�rio
				System.out.println("acao: " +acao);
				switch (acao) { // Com base na a��o, s�o realizadas opera��es diferentes, de acordo com o menu
				case 1:
					String pacote = cadastroConta(scanner); // A interface Acao, determina a a��o requisitada com base em um int, al�m disso, o m�todo cadastro � chamado
					//nesse m�todo, os dados do cadastro s�o obtididos e contatenados numa string com base na entrada feita pelo usu�rio
					outputDados.writeUTF(pacote);	//Envia o pacote ao servidor
					int resposta = inputDados.readInt(); // Recebe resposta do servidor
					if(resposta == 1) { //Confirma se a opera��o foi feita corretamente
						System.out.println("Cadastro Concluido");
						int numeroConta = inputDados.readInt();
						System.out.println("N�mero da conta: " +numeroConta);
					} else if(resposta == 11) {
						System.out.println("Cadastro n�o realizado: Pessoa j� existe");
					}
					outputDados.flush(); 
					break;
				case 2:
					String pacoteLogin = login(scanner); //Recebe pacote de dados respectivos ao login, pelo m�todo de login 
					outputDados.writeUTF(pacoteLogin); // Envia o pacote ao servidor
					int respostaLogin = inputDados.readInt(); //Recebe resposta do servidor
					if(respostaLogin == 3){ // Verifica resposta do servidor
						System.out.println("Login efetuado"); //retorna ao usu�rio 
						estaLogado = true; // Atualiza vari�vel indicando que o usu�rio est� logado
					}
					else if(respostaLogin == 30) 
						System.out.println("Usuario Inexistente");
					else if(respostaLogin == 31)
						System.out.println("Falha na autentica��o");
					outputDados.flush();  
					break;
				case 3:
					if(!estaLogado) { // Se o usu�rio n�o est� logado, n�o � poss�vel entrar nessa se��o
						System.out.println("Por favor, realize o login primeiro");
						break;
					}
					if(numeroContaLogado == null) { //Verifica se algum erro aconteceu, evitando referencia a nulos
						System.out.println("Um erro ocorreu, fa�a login e tente novamente");
						break;
					}
					//Informa��es necess�rias s�o requisitadas ao usu�rio
					System.out.println("Agora, digite a conta de destino");
					String numeroContaDestino = (String) scanner.next();
					System.out.println("Agora, finalmente, digite o valor a ser transferido (utilize v�rgulas somente)");
					double valor = scanner.nextDouble();
					String stringValor = String.valueOf(valor); // O valor em double � 
					String pacoteTransacao = Acao.TRANSACAO+"-"+numeroContaLogado+";"+numeroContaDestino+";"+stringValor; //Pacote de transa��o � preparado, separando a a��o com tra�o e 
					//atributos com ponto e v�rgula
					outputDados.writeUTF(pacoteTransacao); //Envia pacote ao servidor
					int respostaTransferencia = inputDados.readInt(); // L� a resposta do servidor
					System.out.println("Resposta transferencia: "+respostaTransferencia);
					if(respostaTransferencia == 40) //Verifica resposta do servidor, com base em n�meros conhecidos do protocolo criado
						System.out.println("Transfer�ncia bem sucedida!");
					else if(respostaTransferencia == 41) 
						System.out.println("Transfer�ncia mal sucedida, Saldo Insuficiente!");
					else if(respostaTransferencia == 32)
						System.out.println("Conta inexistente, tente novamente");
					outputDados.flush();
					break;
				case 4:
					if(!estaLogado) { // Se o usu�rio n�o est� logado, n�o � poss�vel entrar nessa se��o
						System.out.println("Por favor, realize o login primeiro");
						break;
					}
					if(numeroContaLogado == null) { //Verifica se algum erro aconteceu, evitando referencia a nulos
						System.out.println("Um erro ocorreu, fa�a login e tente novamente");
						break;
					}
					System.out.println("Agora, digite o valor a ser depositado (utilize virgulas somente)"); 
					double valorDeposito = scanner.nextDouble();
					String pacoteDeposito = Acao.DEPOSITO+"-"+numeroContaLogado+";"+String.valueOf(valorDeposito); //Prepara o pacote de deposito, que ir� ser enviado ao servidor
					outputDados.writeUTF(pacoteDeposito); // Envia o pacote ao servidor
					int respostaDeposito = inputDados.readInt(); // L� sua resposta
					if(respostaDeposito == 50) { //Verifica e retorna ao usu�rio, com base em n�mero conhecidos do protocolo criado, os resultados da opera��o
						System.out.println("Deposito bem sucedido");
					} else if(respostaDeposito == 32) {
						System.out.println("Conta inexistente, tente novamente!");
					}
					outputDados.flush();
					break;
				case 5:
					if(!estaLogado) { // Se o usu�rio n�o est� logado, n�o � poss�vel entrar nessa se��o
						System.out.println("Por favor, realize o login primeiro");
						break;
					}
					if(numeroContaLogado == null) { //Verifica se algum erro aconteceu, evitando referencia a nulos
						System.out.println("Um erro ocorreu, fa�a login e tente novamente");
						break;
					}	
					String pacoteTitular = Acao.NOVO_TITULAR+"-"+cadastroPessoa(scanner)+";"+numeroContaLogado; //Cria pacote para adicionar novos titulares
					outputDados.writeUTF(pacoteTitular); //Envia pacote a servidor
					int respostaTitular = inputDados.readInt(); // L� resposta servidor
					if(respostaTitular == 6) 
						System.out.println("Titular cadastrado com sucesso");
					else if(respostaTitular == 32) //Verifica e retorna ao usu�rio, com base em n�mero conhecidos do protocolo criado, os resultados da opera��o
						System.out.println("Conta inexistente");
					else if(respostaTitular == 61) 
						System.out.println("Titular n�o cadastrado, titular j� existe!");
					outputDados.flush();
					break;
				case 6:
					System.out.println("Obrigado, tenha um bom dia!"); //Finaliza sess�o 
					cliente.close();
					return;
				default:
					System.out.println("Digite uma op��o v�lida");
					
				}
				outputDados.close(); //fecha output streams
				inputDados.close(); 
				cliente.close(); //Fecha socket de conex�o com o servidor
			}
	}
	/**
 	 * M�todo que deixa as informa��es recebidas do usu�rio, para o cadastro de pessoa, para o formato do pacote
 	 * @param Nome da pessoa
 	 * @param Vari�vel booleana que identifica se a pessoa � jur�dica 
 	 * @param N�mero de resgistro, sendo este CPF ou CNPJ
 	 * @param Cep 
 	 * @param Rua 
 	 * @param N�meor da casa
 	 * @param Senha do usu�rio
 	 * @return String correspondente aos dados da pessoa, em formato de pacote, segundo o protocolo criado
 	 */
 	public String formataPessoa(String nome, Boolean eJuridica, String numeroRegistro, String cep, String rua, String numero, String senha) { //formata o pacote para o cadastro de contas
		return nome+";"+eJuridica.toString()+";"+numeroRegistro+";"+cep+";"+rua+";"+numero+";"+senha; 
	}
 	/**
 	 * M�todo que realiza o cadastro de contas
 	 * @param Scanner utilizado para recolher dados do usu�rio
 	 * @return String com informa��es de cadastro
 	 * @throws IOException
 	 * @throws NoSuchAlgorithmException
 	 */
	public String cadastroConta(Scanner scanner) throws IOException, NoSuchAlgorithmException {
		String pacote = cadastroPessoa(scanner);
		while(true) { 
			System.out.println("Digite (1) para conta corrente e (2) para poupan�a"); // Escolha entre conta corrente e 
			int escolha = scanner.nextInt();
			if(escolha == 1) {
				pacote = String.valueOf(escolha)+"-"+pacote; //Chama o m�todo de formataPessoa, para retornar o pacote, corretamente
				break;
			} else if(escolha == 2) {
				pacote = String.valueOf(escolha)+"-"+pacote; //Chama o m�todo de formataPessoa, para retornar o pacote, corretamente
				break;
			}
		}
		System.out.println(pacote);
		return pacote; //Retorna o pacote com informa��es de cadastro
	}
	/**
	 * M�todo que realiza a formata��o do cadastro de uma pessoa
	 * @param scanner
	 * @return String das informa��es da pessoa no formato de pacote
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	public String cadastroPessoa(Scanner scanner) throws IOException, NoSuchAlgorithmException {
		System.out.println("Digite o seu nome");
		BufferedReader leitor = new BufferedReader(new InputStreamReader(System.in)); // L� o nome por meio de um bufferedReader, por conta da possibilidade de nomes compostos
		String nome = leitor.readLine(); 
		boolean eJuridica = false; //Inicializa vari�vel eJuridica com o valor false
		while(true) {
			System.out.println("A conta ser� de pessoa f�sica (1) ou jur�dica (2)?, digite a alternativa correspondente");
			int escolha = scanner.nextInt(); // Com base na escolha do usu�rio atualiza vari�vel
			if(escolha == 1) {
				break;
			} else if(escolha == 2) {
				eJuridica = true;
				break;
			} 
		}
		//Recebe informa��es do usu�rio por meio do console
		System.out.println("Digite seu n�mero de CPF ou CNPJ, por favor");
		String numeroRegistro = (String) scanner.next();
		System.out.println("Digite o seu CEP");
		String cep = (String) scanner.next();
		System.out.println("Digite a sua rua"); 
		String rua = leitor.readLine(); // Utiliza tamb�m o bufferedReader para ruas
		System.out.println("Digite o numero da casa");
		String numero = (String) scanner.next();
		System.out.println("Est� quase acabando, digite uma senha para sua nova conta no Banco Virtual (sem espa�os)");
		String senha = (String) scanner.next();
		String senhaMd5 = md5(senha); // A senha � hasheada em md5 para maior seguran�a do usu�rio
		return formataPessoa(nome, eJuridica, numeroRegistro, cep, rua, numero, senhaMd5);
	}
	/**
	 * M�todo que recebe informa��es requeridas ao fazer login
	 * @param Scanner necess�rio para ler informa��es do usu�rio
	 * @return String com informa��es de login, no formato de pacote do protocolo
	 * @throws NoSuchAlgorithmException
	 */
	public String login(Scanner scanner) throws NoSuchAlgorithmException {
		//Recebe infomra��es referentes a login, do usu�rio
		System.out.println("Digite o numero da conta que deseja acessar:");
		numeroContaLogado = (String) scanner.next();
		System.out.println("Digite seu CPF ou CNPJ");
		String registroLogin = (String) scanner.next();
		System.out.println("Digite sua senha");
		String senhaLogin = (String) scanner.next();
		senhaLogin = md5(senhaLogin); // Utiliza o algoritmo md5 na senha para maior seguran�a
		String pacoteLogin = Acao.LOGIN+"-"+registroLogin+";"+senhaLogin+";"+numeroContaLogado; //Cria o pacote de login
		return pacoteLogin; //Retorna o pacote de login
	}
	/**
	 * M�todo utilizado para aplicar o algoritmo md5 a senhas
	 * @param Senha
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public String md5(String senha) throws NoSuchAlgorithmException {
		MessageDigest m=MessageDigest.getInstance("MD5");
		m.update(senha.getBytes(),0,senha.length());
		return new BigInteger(1,m.digest()).toString(16);
	}
}
