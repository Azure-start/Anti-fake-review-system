package com.lwf.raw;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.fisco.bcos.sdk.abi.FunctionReturnDecoder;
import org.fisco.bcos.sdk.abi.TypeReference;
import org.fisco.bcos.sdk.abi.datatypes.Address;
import org.fisco.bcos.sdk.abi.datatypes.Bool;
import org.fisco.bcos.sdk.abi.datatypes.Event;
import org.fisco.bcos.sdk.abi.datatypes.Function;
import org.fisco.bcos.sdk.abi.datatypes.Type;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple1;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.contract.Contract;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.eventsub.EventCallback;
import org.fisco.bcos.sdk.model.CryptoType;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;

@SuppressWarnings("unchecked")
public class AccessControl extends Contract {
    public static final String[] BINARY_ARRAY = {"608060405234801561001057600080fd5b50336000806101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055506107d0806100606000396000f3fe608060405234801561001057600080fd5b50600436106100625760003560e01c806367561d9314610067578063704b6c02146100ab578063cc2e3ee1146100ef578063d5b9221b14610133578063f851a4401461018f578063fe9fbb80146101d9575b600080fd5b6100a96004803603602081101561007d57600080fd5b81019080803573ffffffffffffffffffffffffffffffffffffffff169060200190929190505050610235565b005b6100ed600480360360208110156100c157600080fd5b81019080803573ffffffffffffffffffffffffffffffffffffffff1690602001909291905050506103a4565b005b6101316004803603602081101561010557600080fd5b81019080803573ffffffffffffffffffffffffffffffffffffffff16906020019092919050505061058f565b005b6101756004803603602081101561014957600080fd5b81019080803573ffffffffffffffffffffffffffffffffffffffff1690602001909291905050506106ff565b604051808215151515815260200191505060405180910390f35b61019761071f565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b61021b600480360360208110156101ef57600080fd5b81019080803573ffffffffffffffffffffffffffffffffffffffff169060200190929190505050610744565b604051808215151515815260200191505060405180910390f35b6000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff16146102f7576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004018080602001828103825260098152602001807f4e6f742061646d696e000000000000000000000000000000000000000000000081525060200191505060405180910390fd5b60018060008373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060006101000a81548160ff0219169083151502179055508073ffffffffffffffffffffffffffffffffffffffff167f3213ab8ec2ed122402f038736b41878b69ae9f61f49a7edd0666bce891d29fc96001604051808215151515815260200191505060405180910390a250565b6000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff1614610466576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004018080602001828103825260098152602001807f4e6f742061646d696e000000000000000000000000000000000000000000000081525060200191505060405180910390fd5b600073ffffffffffffffffffffffffffffffffffffffff168173ffffffffffffffffffffffffffffffffffffffff161415610509576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040180806020018281038252600c8152602001807f5a65726f2061646472657373000000000000000000000000000000000000000081525060200191505060405180910390fd5b806000806101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055508073ffffffffffffffffffffffffffffffffffffffff167f7ce7ec0b50378fb6c0186ffb5f48325f6593fcb4ca4386f21861af3129188f5c60405160405180910390a250565b6000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff1614610651576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004018080602001828103825260098152602001807f4e6f742061646d696e000000000000000000000000000000000000000000000081525060200191505060405180910390fd5b6000600160008373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060006101000a81548160ff0219169083151502179055508073ffffffffffffffffffffffffffffffffffffffff167f3213ab8ec2ed122402f038736b41878b69ae9f61f49a7edd0666bce891d29fc96000604051808215151515815260200191505060405180910390a250565b60016020528060005260406000206000915054906101000a900460ff1681565b6000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b6000600160008373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060009054906101000a900460ff16905091905056fea2646970667358221220b447768035c4e7d0066328a9ddfed4dbd875d30bdb9a3b737d0b29a0e181f9b864736f6c634300060a0033"};

    public static final String BINARY = org.fisco.bcos.sdk.utils.StringUtils.joinAll("", BINARY_ARRAY);

    public static final String[] SM_BINARY_ARRAY = {};

    public static final String SM_BINARY = org.fisco.bcos.sdk.utils.StringUtils.joinAll("", SM_BINARY_ARRAY);

    public static final String[] ABI_ARRAY = {"[{\"inputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"internalType\":\"address\",\"name\":\"newAdmin\",\"type\":\"address\"}],\"name\":\"AdminChanged\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"internalType\":\"address\",\"name\":\"contractAddr\",\"type\":\"address\"},{\"indexed\":false,\"internalType\":\"bool\",\"name\":\"status\",\"type\":\"bool\"}],\"name\":\"ContractAuthorized\",\"type\":\"event\"},{\"inputs\":[],\"name\":\"admin\",\"outputs\":[{\"internalType\":\"address\",\"name\":\"\",\"type\":\"address\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"address\",\"name\":\"contractAddr\",\"type\":\"address\"}],\"name\":\"authorizeContract\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"address\",\"name\":\"\",\"type\":\"address\"}],\"name\":\"authorizedContracts\",\"outputs\":[{\"internalType\":\"bool\",\"name\":\"\",\"type\":\"bool\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"address\",\"name\":\"contractAddr\",\"type\":\"address\"}],\"name\":\"isAuthorized\",\"outputs\":[{\"internalType\":\"bool\",\"name\":\"\",\"type\":\"bool\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"address\",\"name\":\"contractAddr\",\"type\":\"address\"}],\"name\":\"revokeContract\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"address\",\"name\":\"newAdmin\",\"type\":\"address\"}],\"name\":\"setAdmin\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]"};

    public static final String ABI = org.fisco.bcos.sdk.utils.StringUtils.joinAll("", ABI_ARRAY);

    public static final String FUNC_ADMIN = "admin";

    public static final String FUNC_AUTHORIZECONTRACT = "authorizeContract";

    public static final String FUNC_AUTHORIZEDCONTRACTS = "authorizedContracts";

    public static final String FUNC_ISAUTHORIZED = "isAuthorized";

    public static final String FUNC_REVOKECONTRACT = "revokeContract";

    public static final String FUNC_SETADMIN = "setAdmin";

    public static final Event ADMINCHANGED_EVENT = new Event("AdminChanged", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}));
    ;

    public static final Event CONTRACTAUTHORIZED_EVENT = new Event("ContractAuthorized", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Bool>() {}));
    ;

    protected AccessControl(String contractAddress, Client client, CryptoKeyPair credential) {
        super(getBinary(client.getCryptoSuite()), contractAddress, client, credential);
    }

    public static String getBinary(CryptoSuite cryptoSuite) {
        return (cryptoSuite.getCryptoTypeConfig() == CryptoType.ECDSA_TYPE ? BINARY : SM_BINARY);
    }

    public List<AdminChangedEventResponse> getAdminChangedEvents(TransactionReceipt transactionReceipt) {
        List<EventValuesWithLog> valueList = extractEventParametersWithLog(ADMINCHANGED_EVENT, transactionReceipt);
        ArrayList<AdminChangedEventResponse> responses = new ArrayList<AdminChangedEventResponse>(valueList.size());
        for (EventValuesWithLog eventValues : valueList) {
            AdminChangedEventResponse typedResponse = new AdminChangedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.newAdmin = (String) eventValues.getIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public void subscribeAdminChangedEvent(String fromBlock, String toBlock, List<String> otherTopics, EventCallback callback) {
        String topic0 = eventEncoder.encode(ADMINCHANGED_EVENT);
        subscribeEvent(ABI,BINARY,topic0,fromBlock,toBlock,otherTopics,callback);
    }

    public void subscribeAdminChangedEvent(EventCallback callback) {
        String topic0 = eventEncoder.encode(ADMINCHANGED_EVENT);
        subscribeEvent(ABI,BINARY,topic0,callback);
    }

    public List<ContractAuthorizedEventResponse> getContractAuthorizedEvents(TransactionReceipt transactionReceipt) {
        List<EventValuesWithLog> valueList = extractEventParametersWithLog(CONTRACTAUTHORIZED_EVENT, transactionReceipt);
        ArrayList<ContractAuthorizedEventResponse> responses = new ArrayList<ContractAuthorizedEventResponse>(valueList.size());
        for (EventValuesWithLog eventValues : valueList) {
            ContractAuthorizedEventResponse typedResponse = new ContractAuthorizedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.contractAddr = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.status = (Boolean) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public void subscribeContractAuthorizedEvent(String fromBlock, String toBlock, List<String> otherTopics, EventCallback callback) {
        String topic0 = eventEncoder.encode(CONTRACTAUTHORIZED_EVENT);
        subscribeEvent(ABI,BINARY,topic0,fromBlock,toBlock,otherTopics,callback);
    }

    public void subscribeContractAuthorizedEvent(EventCallback callback) {
        String topic0 = eventEncoder.encode(CONTRACTAUTHORIZED_EVENT);
        subscribeEvent(ABI,BINARY,topic0,callback);
    }

    public String admin() throws ContractException {
        final Function function = new Function(FUNC_ADMIN, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeCallWithSingleValueReturn(function, String.class);
    }

    public TransactionReceipt authorizeContract(String contractAddr) {
        final Function function = new Function(
                FUNC_AUTHORIZECONTRACT, 
                Arrays.<Type>asList(new Address(contractAddr)),
                Collections.<TypeReference<?>>emptyList());
        return executeTransaction(function);
    }

    public void authorizeContract(String contractAddr, TransactionCallback callback) {
        final Function function = new Function(
                FUNC_AUTHORIZECONTRACT, 
                Arrays.<Type>asList(new Address(contractAddr)),
                Collections.<TypeReference<?>>emptyList());
         asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForAuthorizeContract(String contractAddr) {
        final Function function = new Function(
                FUNC_AUTHORIZECONTRACT, 
                Arrays.<Type>asList(new Address(contractAddr)),
                Collections.<TypeReference<?>>emptyList());
        return createSignedTransaction(function);
    }

    public Tuple1<String> getAuthorizeContractInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function = new Function(FUNC_AUTHORIZECONTRACT, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<String>(

                (String) results.get(0).getValue()
                );
    }

    public Boolean authorizedContracts(String param0) throws ContractException {
        final Function function = new Function(FUNC_AUTHORIZEDCONTRACTS, 
                Arrays.<Type>asList(new Address(param0)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeCallWithSingleValueReturn(function, Boolean.class);
    }

    public Boolean isAuthorized(String contractAddr) throws ContractException {
        final Function function = new Function(FUNC_ISAUTHORIZED, 
                Arrays.<Type>asList(new Address(contractAddr)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeCallWithSingleValueReturn(function, Boolean.class);
    }

    public TransactionReceipt revokeContract(String contractAddr) {
        final Function function = new Function(
                FUNC_REVOKECONTRACT, 
                Arrays.<Type>asList(new Address(contractAddr)),
                Collections.<TypeReference<?>>emptyList());
        return executeTransaction(function);
    }

    public void revokeContract(String contractAddr, TransactionCallback callback) {
        final Function function = new Function(
                FUNC_REVOKECONTRACT, 
                Arrays.<Type>asList(new Address(contractAddr)),
                Collections.<TypeReference<?>>emptyList());
        asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForRevokeContract(String contractAddr) {
        final Function function = new Function(
                FUNC_REVOKECONTRACT, 
                Arrays.<Type>asList(new Address(contractAddr)),
                Collections.<TypeReference<?>>emptyList());
        return createSignedTransaction(function);
    }

    public Tuple1<String> getRevokeContractInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function = new Function(FUNC_REVOKECONTRACT, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<String>(

                (String) results.get(0).getValue()
                );
    }

    public TransactionReceipt setAdmin(String newAdmin) {
        final Function function = new Function(
                FUNC_SETADMIN, 
                Arrays.<Type>asList(new Address(newAdmin)),
                Collections.<TypeReference<?>>emptyList());
        return executeTransaction(function);
    }

    public void setAdmin(String newAdmin, TransactionCallback callback) {
        final Function function = new Function(
                FUNC_SETADMIN, 
                Arrays.<Type>asList(new Address(newAdmin)),
                Collections.<TypeReference<?>>emptyList());
        asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForSetAdmin(String newAdmin) {
        final Function function = new Function(
                FUNC_SETADMIN, 
                Arrays.<Type>asList(new Address(newAdmin)),
                Collections.<TypeReference<?>>emptyList());
        return createSignedTransaction(function);
    }

    public Tuple1<String> getSetAdminInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function = new Function(FUNC_SETADMIN, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<String>(

                (String) results.get(0).getValue()
                );
    }

    public static AccessControl load(String contractAddress, Client client, CryptoKeyPair credential) {
        return new AccessControl(contractAddress, client, credential);
    }

    public static AccessControl deploy(Client client, CryptoKeyPair credential) throws ContractException {
        return deploy(AccessControl.class, client, credential, getBinary(client.getCryptoSuite()), "");
    }

    public static class AdminChangedEventResponse {
        public TransactionReceipt.Logs log;

        public String newAdmin;
    }

    public static class ContractAuthorizedEventResponse {
        public TransactionReceipt.Logs log;

        public String contractAddr;

        public Boolean status;
    }
}
