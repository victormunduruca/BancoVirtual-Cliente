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
import java.util.Scanner;

import br.uefs.ecomp.cliente.exceptions.PessoaExistenteException;
import br.uefs.ecomp.cliente.model.Acao;

public class Cliente {

	private static boolean estaLogado;
	private static String numeroContaLogado;
	public Cliente() {
		estaLogado = false;
	}
 	public static void main(String[] args) throws UnknownHostException, IOException, NoSuchAlgorithmException {
 		try{
 			executa(); 
 		} catch (ConnectException e) {
 			System.out.println("Erro, não conseguiu conectar ao servidor");
 		}
		return;
	}
 	
 	public static String formataPessoa(String nome, Boolean eJuridica, String numeroRegistro, String cep, String rua, String numero, String senha) { //formata o pacote para o cadastro de contas
		return nome+";"+eJuridica.toString()+";"+numeroRegistro+";"+cep+";"+rua+";"+numero+";"+senha; 
	}
 	
	public static void executa() throws IOException, NoSuchAlgorithmException {
		
		Scanner scanner = new Scanner(System.in);
		
		
			while(true) {
				Socket cliente = new Socket("192.168.1.8", 12346);
				System.out.println("O cliente se conectou ao servidor!");
				DataOutputStream outputDados = new DataOutputStream(cliente.getOutputStream()); //streams de dados
				DataInputStream inputDados = new DataInputStream(cliente.getInputStream());
				System.out.println("Escolha a opção desejada\n1 - Cadastrar Conta\n2 - Fazer login\n3- Fazer transferencia\n4- Fazer deposito\n5- Cadastrar novo titular\n6-Encerrar sessão\n");
				int acao = scanner.nextInt();
				System.out.println("acao: " +acao);
				switch (acao) {
				case 1:
					String pacote = Acao.CADASTRAR_CONTA_CORRENTE+"-"+cadastro(scanner);
					outputDados.writeUTF(pacote);	//envia o pacote ao servidor
					int resposta = inputDados.readInt();
					if(resposta == 1) { //confirma se a operação foi feita corretamente
						System.out.println("Cadastro Concluido");
					} else if(resposta == 10) {
						System.out.println("Cadastro não realizado: Pessoa já existe");
					}
					outputDados.flush();
					break;
				case 2:
					String pacoteLogin = login(scanner);
					outputDados.writeUTF(pacoteLogin);
					int respostaLogin = inputDados.readInt();
					if(respostaLogin == 3){
						System.out.println("Login efetuado");
						estaLogado = true;
					}
					else if(respostaLogin == 30)
						System.out.println("Usuario Inexistente");
					else if(respostaLogin == 31)
						System.out.println("Falha na autenticação");
					outputDados.flush();  
					break;
				case 3:
					if(!estaLogado) {
						System.out.println("Por favor, realize o login primeiro");
						break;
					}
					System.out.println("Por favor, digite o número da conta de origem");
					String  numeroContaOrigem = (String) scanner.next();
					System.out.println("Agora, digite a conta de destino");
					String numeroContaDestino = (String) scanner.next();
					System.out.println("Agora, finalmente, digete o valor a ser transferido");
					double valor = scanner.nextDouble();
					String stringValor = String.valueOf(valor);
					System.out.println("o double convertido ficou:" +stringValor);
					String pacoteTransacao = Acao.TRANSACAO+"-"+numeroContaOrigem+";"+numeroContaDestino+";"+stringValor;
					outputDados.writeUTF(pacoteTransacao);
					int respostaTransferencia = inputDados.readInt();
					if(respostaTransferencia == 40) 
						System.out.println("Transferência bem sucedida!");
					else if(respostaTransferencia == 41) 
						System.out.println("Transferência mal sucedida, Saldo Insuficiente!");
					else if(respostaTransferencia == 42)
						System.out.println("Um erro aconteceu, tente novamente");
					System.out.println("resposta: " +respostaTransferencia);
					break;
				case 4:
					if(!estaLogado) {
						System.out.println("Por favor, realize o login primeiro");
						break;
					}
					System.out.println("Por favor, digite o número da conta para qual deseja depositar");
					String numeroConta = (String) scanner.next();
					System.out.println("Agora, digite o valor a ser depositado (utilize virgulas)");
					double valorDeposito = scanner.nextDouble();
					String pacoteDeposito = Acao.DEPOSITO+"-"+numeroConta+";"+String.valueOf(valorDeposito);
					outputDados.writeUTF(pacoteDeposito);
					int respostaDeposito = inputDados.readInt();
					if(respostaDeposito == 50) {
						System.out.println("Deposito bem sucedido");
					} else if(respostaDeposito == 32) {
						System.out.println("Conta inexistente, tente novamente!");
					}
					break;
				case 5:
					if(!estaLogado) {
						System.out.println("Por favor, realize o login primeiro");
						break;
					}
					if(numeroContaLogado == null)
						System.out.println("Um erro ocorreu, faça login e tente novamente");
					String pacoteTitular = Acao.NOVO_TITULAR+"-"+cadastro(scanner)+";"+numeroContaLogado;
					System.out.println("Pacote adicionar titular: " +pacoteTitular);
					outputDados.writeUTF(pacoteTitular);
					int respostaTitular = inputDados.readInt();
					if(respostaTitular == 32) 
						System.out.println("Conta inexistente");
					else if(respostaTitular == 6) 
						System.out.println("Titular cadastrado com sucesso");
				case 6:
					System.out.println("Obrigado, tenha um bom dia!");
					cliente.close();
					return;
				default:
					System.out.println("Digite uma opção válida");
					
				}
				outputDados.close(); //fecha output streams
				inputDados.close();
				cliente.close();
			}
	}
	
	public static String cadastro(Scanner scanner) throws IOException, NoSuchAlgorithmException {
		String pacote;
		
		System.out.println("Digite o seu nome");
		BufferedReader leitor = new BufferedReader(new InputStreamReader(System.in));
		String nome = leitor.readLine();// Tratar exception
		boolean eJuridica = false;
		while(true) {
			System.out.println("A conta será de pessoa física (1) ou jurídica (2)?, digite a alternativa correspondente");
			int escolha = scanner.nextInt();
			if(escolha == 1) {
				break;
			} else if(escolha == 2) {
				eJuridica = true;
				break;
			} 
		}
		System.out.println("Digite seu número de CPF ou CNPJ, por favor");
		String numeroRegistro = (String) scanner.next();
		System.out.println("Digite o seu CEP");
		String cep = (String) scanner.next();
		System.out.println("Digite a sua rua"); //tratar para ruas
		String rua = leitor.readLine();
		System.out.println("Digite o numero da casa");
		String numero = (String) scanner.next();
		System.out.println("Está quase acabando, digite uma senha para sua nova conta no Banco Virtual (sem espaços)");
		String senha = (String) scanner.next();
		String senhaMd5 = md5(senha);
		while(true) {
			System.out.println("Digite (1) para conta corrente e (2) para jurídica");
			int escolha = scanner.nextInt();
			if(escolha == 1) {
				break;
			} else if(escolha == 2) {
				
				break;
			}
		}
		pacote = formataPessoa(nome, eJuridica, numeroRegistro, cep, rua, numero, senhaMd5);
		System.out.println(pacote);
	
		return pacote;
	}
	
	public static String login(Scanner scanner) throws NoSuchAlgorithmException {
		System.out.println("Digite o numero da conta que deseja acessar:");
		numeroContaLogado = (String) scanner.next();
		System.out.println("Digite seu CPF ou CNPJ");
		String registroLogin = (String) scanner.next();
		System.out.println("Digite sua senha");
		String senhaLogin = (String) scanner.next();
		senhaLogin = md5(senhaLogin);
		String pacoteLogin = Acao.LOGIN+"-"+registroLogin+";"+senhaLogin+";"+numeroContaLogado;
	
		return pacoteLogin;
	}
	
	public static String md5(String senha) throws NoSuchAlgorithmException {
		 MessageDigest m=MessageDigest.getInstance("MD5");
	       m.update(senha.getBytes(),0,senha.length());
	      // System.out.println("MD5: "+new BigInteger(1,m.digest()).toString(16));
	      return new BigInteger(1,m.digest()).toString(16);
	}
}
