package br.uefs.ecomp.cliente.model;
/**
 * Interface utilizada para facilitar a implementação do protocolo criado
 * @author victo
 *
 */
public interface Acao {
	int CADASTRAR_CONTA_CORRENTE = 1; 
	int CADASTRAR_CONTA_POUPANCA = 2;
	int LOGIN = 3;
	int TRANSACAO = 4;
	int DEPOSITO = 5;
	int NOVO_TITULAR = 6;
}
