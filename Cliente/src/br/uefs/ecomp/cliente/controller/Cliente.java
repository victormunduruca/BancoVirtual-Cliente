package br.uefs.ecomp.cliente.controller;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

import br.uefs.ecomp.cliente.exceptions.PessoaExistenteException;
import br.uefs.ecomp.cliente.model.Acao;

public class Cliente {
	public static void main(String[] args) throws UnknownHostException, IOException, NoSuchAlgorithmException {
		 // conecta com o servidor
		executa(); //recebe pacote com base em menu
	}
	
	public static String formataCadastroConta(Integer acao, String nome, Boolean eJuridica, String numeroRegistro, String cep, String rua, String numero, String senha) { //formata o pacote para o cadastro de contas
		return acao.toString()+"-"+nome+";"+eJuridica.toString()+";"+numeroRegistro+";"+cep+";"+rua+";"+numero+";"+senha; 
	}
	
	public static void executa() throws IOException, NoSuchAlgorithmException {
		
		Scanner scanner = new Scanner(System.in);
		
		
			while(true) {
				Socket cliente = new Socket("192.168.1.8", 12346);
				System.out.println("O cliente se conectou ao servidor!");
				DataOutputStream outputDados = new DataOutputStream(cliente.getOutputStream()); //streams de dados
				DataInputStream inputDados = new DataInputStream(cliente.getInputStream());
				System.out.println("Escolha a op��o desejada\n1 - Cadastrar Conta\n2 - Fazer login\nPressione qualquer tecla pra sair");
				int acao = scanner.nextInt();
				System.out.println("acao: " +acao);
				switch (acao) {
				case 1:
					String pacote = cadastro(scanner);
					outputDados.writeUTF(pacote);	//envia o pacote ao servidor
					int resposta = inputDados.readInt();
					if(resposta == 1) { //confirma se a opera��o foi feita corretamente
						System.out.println("Cadastro Concluido");
					} else if(resposta == 10) {
						System.out.println("Cadastro n�o realizado: Pessoa j� existe");
					}
					outputDados.flush();
//					outputDados.close(); //fecha output streams
//					inputDados.close();
					break;
				case 2:
					String pacoteLogin = login(scanner);
					outputDados.writeUTF(pacoteLogin);
					int respostaLogin = inputDados.readInt();
					if(respostaLogin == 3){
						System.out.println("Login efetuado");
						String pacoteAcaoLogin = subMenuLogin(scanner);
						outputDados.writeUTF(pacoteAcaoLogin);
						int respostaAcaoLogin = inputDados.readInt();
						switch (respostaAcaoLogin) {
						case 50:
							System.out.println("Deposito bem sucedido");
						case 40:
							System.out.println("Transacao bem sucedida!");
							break;
						case 41: 
							System.out.println("Transa��o mal sucedida: Saldo insuficiente");
							break;
						case 42:
							System.out.println("Um erro aconteceu, tente novamente");
							break;
						case 32:
							System.out.println("Conta inexistente!");
						default:
							break;
						}
					}
					else if(respostaLogin == 30)
						System.out.println("Usuario Inexistente");
					else if(respostaLogin == 31)
						System.out.println("Falha na autentica��o");
					outputDados.flush();  
//					outputDados.close(); //fecha output streams
//					inputDados.close();
					break;
				default:
					System.out.println("Digite uma op��o v�lida");
					break;
				}
				outputDados.close(); //fecha output streams
				inputDados.close();
				cliente.close();
			}
			
	}
	private static String subMenuLogin(Scanner scanner) {
		System.out.println("Escolha uma das op��es:\n1-Fazer transa��o\n2- Fazer dep�sito");
		int acao = scanner.nextInt();
		switch (acao) {
		case 1:
			System.out.println("Por favor, digite o n�mero da conta de origem");
			String  numeroContaOrigem = (String) scanner.next();
			System.out.println("Agora, digite a conta de destino");
			String numeroContaDestino = (String) scanner.next();
			System.out.println("Agora, finalmente, digete o valor a ser transferido");
			double valor = scanner.nextDouble();
			String stringValor = String.valueOf(valor);
			System.out.println("o double convertido ficou:" +stringValor);
			String pacoteTransacao = Acao.TRANSACAO+"-"+numeroContaOrigem+";"+numeroContaDestino+";"+stringValor;
			return pacoteTransacao;
		case 2:
			System.out.println("Por favor, digite o n�mero da conta para qual deseja depositar");
			String numeroConta = (String) scanner.next();
			System.out.println("Agora, digite o valor a ser depositado (utilize ponto para as virgulas)");
			double valorDeposito = scanner.nextDouble();
			String pacoteDeposito = Acao.DEPOSITO+"-"+numeroConta+";"+String.valueOf(valorDeposito);
		default:
			break;
		}
		return null;
	}

	public static String cadastro(Scanner scanner) throws IOException, NoSuchAlgorithmException {
		String pacote;
		
		System.out.println("Digite o seu nome");
		BufferedReader leitor = new BufferedReader(new InputStreamReader(System.in));
		String nome = leitor.readLine();// Tratar exception
		boolean eJuridica = false;
		while(true) {
			System.out.println("A conta ser� de pessoa f�sica (1) ou jur�dica (2)?, digite a alternativa correspondente");
			int escolha = scanner.nextInt();
			if(escolha == 1) {
				break;
			} else if(escolha == 2) {
				eJuridica = true;
				break;
			} 
		}
		System.out.println("Digite seu n�mero de CPF ou CNPJ, por favor");
		String numeroRegistro = (String) scanner.next();
		System.out.println("Digite o seu CEP");
		String cep = (String) scanner.next();
		System.out.println("Digite a sua rua"); //tratar para ruas
		String rua = leitor.readLine();
		System.out.println("Digite o numero da casa");
		String numero = (String) scanner.next();
		System.out.println("Est� quase acabando, digite uma senha para sua nova conta no Banco Virtual (sem espa�os)");
		String senha = (String) scanner.next();
		String senhaMd5 = md5(senha);
		while(true) {
			System.out.println("Digite (1) para conta corrente e (2) para jur�dica");
			int escolha = scanner.nextInt();
			if(escolha == 1) {
				pacote = formataCadastroConta(Acao.CADASTRAR_CONTA_CORRENTE, nome, eJuridica, numeroRegistro, cep, rua, numero, senhaMd5);
				break;
			} else if(escolha == 2) {
				pacote = formataCadastroConta(Acao.CADASTRAR_CONTA_POUPANCA, nome, eJuridica, numeroRegistro, cep, rua, numero, senhaMd5);
				break;
			}
		}
		System.out.println(pacote);
	
		return pacote;
	}
	
	public static String login(Scanner scanner) throws NoSuchAlgorithmException {
		System.out.println("Digite o numero da conta que deseja acessar:");
		String numeroConta = (String) scanner.next();
		System.out.println("Digite seu CPF ou CNPJ");
		String registroLogin = (String) scanner.next();
		System.out.println("Digite sua senha");
		String senhaLogin = (String) scanner.next();
		senhaLogin = md5(senhaLogin);
		String pacoteLogin = Acao.LOGIN+"-"+registroLogin+";"+senhaLogin+";"+numeroConta;
	
		return pacoteLogin;
	}
	public static String md5(String senha) throws NoSuchAlgorithmException {
		 MessageDigest m=MessageDigest.getInstance("MD5");
	       m.update(senha.getBytes(),0,senha.length());
	      // System.out.println("MD5: "+new BigInteger(1,m.digest()).toString(16));
	      return new BigInteger(1,m.digest()).toString(16);
	}
}
