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
 * Essa classe realiza a comunicação entre o cliente e servidor, recebendo os dados, preparando-os e enviando.
 */

public class Cliente {

	private static boolean estaLogado; // Váriavel que indica se o cliente está logado ou não
	private static String numeroContaLogado; // 
	public Cliente() {
		estaLogado = false;
	}
 	public static void main(String[] args) throws UnknownHostException, IOException, NoSuchAlgorithmException {
 		try{
 			new Cliente().executa(); //método que inicializa as ações do cliente
 		} catch (ConnectException e) {
 			System.out.println("Erro, não conseguiu conectar ao servidor");
 		} 
		return;
	}
 
 	/**
 	 * Método que realiza as ações de cliente, por uma interface de menu. Onde os pacotes são preenchidos, enviados e possíveis respostas do servidor informadas ao usuário
 	 * @throws IOException
 	 * @throws NoSuchAlgorithmException
 	 */
	public void executa() throws IOException, NoSuchAlgorithmException {
		
		Scanner scanner = new Scanner(System.in);
		
		
			while(true) { // Laço de repetição utilizado para caso o usuário deseje realizar mais de uma operação
				Socket cliente = new Socket("192.168.1.8", 12346); // Socket é conectado ao servidor, por um ip e porta especificados
				System.out.println("O cliente se conectou ao servidor!"); // Mensagem de conexão é passada ao usuário
				DataOutputStream outputDados = new DataOutputStream(cliente.getOutputStream()); // Saida de dados, com base no socket do servidor
				DataInputStream inputDados = new DataInputStream(cliente.getInputStream()); // Entrada de dados com base no socket do servidor
				System.out.println("Escolha a opção desejada\n1 - Cadastrar Conta\n2 - Fazer login\n3- Fazer transferencia\n4- Fazer deposito\n5- Cadastrar novo titular\n6-Encerrar sessão"); //Menu com as escolhas mostradas ao usuário
				int acao = scanner.nextInt(); // A ação desejada é obtida do usuário
				System.out.println("acao: " +acao);
				switch (acao) { // Com base na ação, são realizadas operações diferentes, de acordo com o menu
				case 1:
					String pacote = cadastroConta(scanner); // A interface Acao, determina a ação requisitada com base em um int, além disso, o método cadastro é chamado
					//nesse método, os dados do cadastro são obtididos e contatenados numa string com base na entrada feita pelo usuário
					outputDados.writeUTF(pacote);	//Envia o pacote ao servidor
					int resposta = inputDados.readInt(); // Recebe resposta do servidor
					if(resposta == 1) { //Confirma se a operação foi feita corretamente
						System.out.println("Cadastro Concluido");
						int numeroConta = inputDados.readInt();
						System.out.println("Número da conta: " +numeroConta);
					} else if(resposta == 11) {
						System.out.println("Cadastro não realizado: Pessoa já existe");
					}
					outputDados.flush(); 
					break;
				case 2:
					String pacoteLogin = login(scanner); //Recebe pacote de dados respectivos ao login, pelo método de login 
					outputDados.writeUTF(pacoteLogin); // Envia o pacote ao servidor
					int respostaLogin = inputDados.readInt(); //Recebe resposta do servidor
					if(respostaLogin == 3){ // Verifica resposta do servidor
						System.out.println("Login efetuado"); //retorna ao usuário 
						estaLogado = true; // Atualiza variável indicando que o usuário está logado
					}
					else if(respostaLogin == 30) 
						System.out.println("Usuario Inexistente");
					else if(respostaLogin == 31)
						System.out.println("Falha na autenticação");
					outputDados.flush();  
					break;
				case 3:
					if(!estaLogado) { // Se o usuário não está logado, não é possível entrar nessa seção
						System.out.println("Por favor, realize o login primeiro");
						break;
					}
					if(numeroContaLogado == null) { //Verifica se algum erro aconteceu, evitando referencia a nulos
						System.out.println("Um erro ocorreu, faça login e tente novamente");
						break;
					}
					//Informações necessárias são requisitadas ao usuário
					System.out.println("Agora, digite a conta de destino");
					String numeroContaDestino = (String) scanner.next();
					System.out.println("Agora, finalmente, digite o valor a ser transferido");
					double valor = scanner.nextDouble();
					String stringValor = String.valueOf(valor); // O valor em double é 
					String pacoteTransacao = Acao.TRANSACAO+"-"+numeroContaLogado+";"+numeroContaDestino+";"+stringValor; //Pacote de transação é preparado, separando a ação com traço e 
					//atributos com ponto e vírgula
					outputDados.writeUTF(pacoteTransacao); //Envia pacote ao servidor
					int respostaTransferencia = inputDados.readInt(); // Lê a resposta do servidor
					if(respostaTransferencia == 40) //Verifica resposta do servidor, com base em números conhecidos do protocolo criado
						System.out.println("Transferência bem sucedida!");
					else if(respostaTransferencia == 41) 
						System.out.println("Transferência mal sucedida, Saldo Insuficiente!");
					else if(respostaTransferencia == 42)
						System.out.println("Um erro aconteceu, tente novamente");
					break;
				case 4:
					if(!estaLogado) { // Se o usuário não está logado, não é possível entrar nessa seção
						System.out.println("Por favor, realize o login primeiro");
						break;
					}
					if(numeroContaLogado == null) { //Verifica se algum erro aconteceu, evitando referencia a nulos
						System.out.println("Um erro ocorreu, faça login e tente novamente");
						break;
					}
					System.out.println("Agora, digite o valor a ser depositado (utilize virgulas)"); 
					double valorDeposito = scanner.nextDouble();
					String pacoteDeposito = Acao.DEPOSITO+"-"+numeroContaLogado+";"+String.valueOf(valorDeposito); //Prepara o pacote de deposito, que irá ser enviado ao servidor
					outputDados.writeUTF(pacoteDeposito); // Envia o pacote ao servidor
					int respostaDeposito = inputDados.readInt(); // Lê sua resposta
					if(respostaDeposito == 50) { //Verifica e retorna ao usuário, com base em número conhecidos do protocolo criado, os resultados da operação
						System.out.println("Deposito bem sucedido");
					} else if(respostaDeposito == 32) {
						System.out.println("Conta inexistente, tente novamente!");
					}
					break;
				case 5:
					if(!estaLogado) { // Se o usuário não está logado, não é possível entrar nessa seção
						System.out.println("Por favor, realize o login primeiro");
						break;
					}
					if(numeroContaLogado == null) { //Verifica se algum erro aconteceu, evitando referencia a nulos
						System.out.println("Um erro ocorreu, faça login e tente novamente");
						break;
					}	
					String pacoteTitular = Acao.NOVO_TITULAR+"-"+cadastroPessoa(scanner)+";"+numeroContaLogado; //Cria pacote para adicionar novos titulares
					outputDados.writeUTF(pacoteTitular); //Envia pacote a servidor
					int respostaTitular = inputDados.readInt(); // Lê resposta servidor
					if(respostaTitular == 6) 
						System.out.println("Titular cadastrado com sucesso");
					else if(respostaTitular == 32) //Verifica e retorna ao usuário, com base em número conhecidos do protocolo criado, os resultados da operação
						System.out.println("Conta inexistente");
					else if(respostaTitular == 61) 
						System.out.println("Titular não cadastrado, titular já existe!");
					break;
				case 6:
					System.out.println("Obrigado, tenha um bom dia!"); //Finaliza sessão 
					cliente.close();
					return;
				default:
					System.out.println("Digite uma opção válida");
					
				}
				outputDados.close(); //fecha output streams
				inputDados.close(); 
				cliente.close(); //Fecha socket de conexão com o servidor
			}
	}
	/**
 	 * Método que deixa as informações recebidas do usuário, para o cadastro de pessoa, para o formato do pacote
 	 * @param Nome da pessoa
 	 * @param Variável booleana que identifica se a pessoa é jurídica 
 	 * @param Número de resgistro, sendo este CPF ou CNPJ
 	 * @param Cep 
 	 * @param Rua 
 	 * @param Númeor da casa
 	 * @param Senha do usuário
 	 * @return String correspondente aos dados da pessoa, em formato de pacote, segundo o protocolo criado
 	 */
 	public String formataPessoa(String nome, Boolean eJuridica, String numeroRegistro, String cep, String rua, String numero, String senha) { //formata o pacote para o cadastro de contas
		return nome+";"+eJuridica.toString()+";"+numeroRegistro+";"+cep+";"+rua+";"+numero+";"+senha; 
	}
 	/**
 	 * Método que realiza o cadastro de contas
 	 * @param Scanner utilizado para recolher dados do usuário
 	 * @return String com informações de cadastro
 	 * @throws IOException
 	 * @throws NoSuchAlgorithmException
 	 */
	public String cadastroConta(Scanner scanner) throws IOException, NoSuchAlgorithmException {
		String pacote = cadastroPessoa(scanner);
		while(true) { 
			System.out.println("Digite (1) para conta corrente e (2) para poupança"); // Escolha entre conta corrente e 
			int escolha = scanner.nextInt();
			if(escolha == 1) {
				pacote = String.valueOf(escolha)+"-"+pacote; //Chama o método de formataPessoa, para retornar o pacote, corretamente
				break;
			} else if(escolha == 2) {
				pacote = String.valueOf(escolha)+"-"+pacote; //Chama o método de formataPessoa, para retornar o pacote, corretamente
				break;
			}
		}
		System.out.println(pacote);
		return pacote; //Retorna o pacote com informações de cadastro
	}
	public String cadastroPessoa(Scanner scanner) throws IOException, NoSuchAlgorithmException {
		System.out.println("Digite o seu nome");
		BufferedReader leitor = new BufferedReader(new InputStreamReader(System.in)); // Lê o nome por meio de um bufferedReader, por conta da possibilidade de nomes compostos
		String nome = leitor.readLine(); 
		boolean eJuridica = false; //Inicializa variável eJuridica com o valor false
		while(true) {
			System.out.println("A conta será de pessoa física (1) ou jurídica (2)?, digite a alternativa correspondente");
			int escolha = scanner.nextInt(); // Com base na escolha do usuário atualiza variável
			if(escolha == 1) {
				break;
			} else if(escolha == 2) {
				eJuridica = true;
				break;
			} 
		}
		//Recebe informações do usuário por meio do console
		System.out.println("Digite seu número de CPF ou CNPJ, por favor");
		String numeroRegistro = (String) scanner.next();
		System.out.println("Digite o seu CEP");
		String cep = (String) scanner.next();
		System.out.println("Digite a sua rua"); 
		String rua = leitor.readLine(); // Utiliza também o bufferedReader para ruas
		System.out.println("Digite o numero da casa");
		String numero = (String) scanner.next();
		System.out.println("Está quase acabando, digite uma senha para sua nova conta no Banco Virtual (sem espaços)");
		String senha = (String) scanner.next();
		String senhaMd5 = md5(senha); // A senha é hasheada em md5 para maior segurança do usuário
		return formataPessoa(nome, eJuridica, numeroRegistro, cep, rua, numero, senhaMd5);
	}
	/**
	 * Método que recebe informações requeridas ao fazer login
	 * @param Scanner necessário para ler informações do usuário
	 * @return String com informações de login, no formato de pacote do protocolo
	 * @throws NoSuchAlgorithmException
	 */
	public String login(Scanner scanner) throws NoSuchAlgorithmException {
		//Recebe infomrações referentes a login, do usuário
		System.out.println("Digite o numero da conta que deseja acessar:");
		numeroContaLogado = (String) scanner.next();
		System.out.println("Digite seu CPF ou CNPJ");
		String registroLogin = (String) scanner.next();
		System.out.println("Digite sua senha");
		String senhaLogin = (String) scanner.next();
		senhaLogin = md5(senhaLogin); // Utiliza o algoritmo md5 na senha para maior segurança
		String pacoteLogin = Acao.LOGIN+"-"+registroLogin+";"+senhaLogin+";"+numeroContaLogado; //Cria o pacote de login
		return pacoteLogin; //Retorna o pacote de login
	}
	/**
	 * Método utilizado para aplicar o algoritmo md5 a senhas
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
