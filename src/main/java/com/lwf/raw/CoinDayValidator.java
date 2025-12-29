package com.lwf.raw;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.fisco.bcos.sdk.abi.FunctionEncoder;
import org.fisco.bcos.sdk.abi.FunctionReturnDecoder;
import org.fisco.bcos.sdk.abi.TypeReference;
import org.fisco.bcos.sdk.abi.datatypes.Address;
import org.fisco.bcos.sdk.abi.datatypes.Bool;
import org.fisco.bcos.sdk.abi.datatypes.Event;
import org.fisco.bcos.sdk.abi.datatypes.Function;
import org.fisco.bcos.sdk.abi.datatypes.Type;
import org.fisco.bcos.sdk.abi.datatypes.generated.Uint256;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple1;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple2;
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
public class CoinDayValidator extends Contract {
    public static final String[] BINARY_ARRAY = {"6080604052600060035534801561001557600080fd5b50604051610ef7380380610ef78339818101604052604081101561003857600080fd5b810190808051906020019092919080519060200190929190505050816000806101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff160217905550806003819055505050610e4c806100ab6000396000f3fe608060405234801561001057600080fd5b50600436106100b45760003560e01c8063c891091311610071578063c891091314610241578063ce73c830146102a0578063d7b95c6d146102be578063ef1b4ef51461031a578063f5dac8f114610364578063fc0c546a146103bc576100b4565b806313007d55146100b957806317ec0d7b1461010357806319129e5a1461013157806323a78eb5146101755780637ee97dd2146101b95780639e9f4ad2146101fd575b600080fd5b6100c1610406565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b61012f6004803603602081101561011957600080fd5b810190808035906020019092919050505061042c565b005b6101736004803603602081101561014757600080fd5b81019080803573ffffffffffffffffffffffffffffffffffffffff169060200190929190505050610579565b005b6101b76004803603602081101561018b57600080fd5b81019080803573ffffffffffffffffffffffffffffffffffffffff169060200190929190505050610681565b005b6101fb600480360360208110156101cf57600080fd5b81019080803573ffffffffffffffffffffffffffffffffffffffff169060200190929190505050610813565b005b61023f6004803603602081101561021357600080fd5b81019080803573ffffffffffffffffffffffffffffffffffffffff16906020019092919050505061099a565b005b6102836004803603602081101561025757600080fd5b81019080803573ffffffffffffffffffffffffffffffffffffffff169060200190929190505050610be6565b604051808381526020018281526020019250505060405180910390f35b6102a8610c0a565b6040518082815260200191505060405180910390f35b610300600480360360208110156102d457600080fd5b81019080803573ffffffffffffffffffffffffffffffffffffffff169060200190929190505050610c10565b604051808215151515815260200191505060405180910390f35b610322610c27565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b6103a66004803603602081101561037a57600080fd5b81019080803573ffffffffffffffffffffffffffffffffffffffff169060200190929190505050610c4d565b6040518082815260200191505060405180910390f35b6103c4610dd7565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b600160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b600160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1663f851a4406040518163ffffffff1660e01b815260040160206040518083038186803b15801561049457600080fd5b505afa1580156104a8573d6000803e3d6000fd5b505050506040513d60208110156104be57600080fd5b810190808051906020019092919050505073ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff161461056f576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004018080602001828103825260098152602001807f4e6f742061646d696e000000000000000000000000000000000000000000000081525060200191505060405180910390fd5b8060038190555050565b600073ffffffffffffffffffffffffffffffffffffffff16600160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff161461063d576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040180806020018281038252600b8152602001807f416c72656164792073657400000000000000000000000000000000000000000081525060200191505060405180910390fd5b80600160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555050565b6000600460008373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000206001015414156108105760405180604001604052806000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff166370a08231846040518263ffffffff1660e01b8152600401808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060206040518083038186803b15801561077557600080fd5b505afa158015610789573d6000803e3d6000fd5b505050506040513d602081101561079f57600080fd5b8101908080519060200190929190505050815260200142815250600460008373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060008201518160000155602082015181600101559050505b50565b600160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1663f851a4406040518163ffffffff1660e01b815260040160206040518083038186803b15801561087b57600080fd5b505afa15801561088f573d6000803e3d6000fd5b505050506040513d60208110156108a557600080fd5b810190808051906020019092919050505073ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff1614610956576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004018080602001828103825260098152602001807f4e6f742061646d696e000000000000000000000000000000000000000000000081525060200191505060405180910390fd5b80600260006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555050565b600260009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff1614610a5d576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040180806020018281038252600e8152602001807f4e6f7420526576696577436f726500000000000000000000000000000000000081525060200191505060405180910390fd5b60405180604001604052806000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff166370a08231846040518263ffffffff1660e01b8152600401808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060206040518083038186803b158015610b0657600080fd5b505afa158015610b1a573d6000803e3d6000fd5b505050506040513d6020811015610b3057600080fd5b8101908080519060200190929190505050815260200142815250600460008373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060008201518160000155602082015181600101559050508073ffffffffffffffffffffffffffffffffffffffff167f25e48e56407dba9206dbb9a9c8c65a22b7bc054fba7eec91dd9a75ea624ccef760405160405180910390a250565b60046020528060005260406000206000915090508060000154908060010154905082565b60035481565b6000600354610c1e83610c4d565b10159050919050565b600260009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b6000610c57610dfc565b600460008473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060405180604001604052908160008201548152602001600182015481525050905060008060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff166370a08231856040518263ffffffff1660e01b8152600401808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060206040518083038186803b158015610d5757600080fd5b505afa158015610d6b573d6000803e3d6000fd5b505050506040513d6020811015610d8157600080fd5b81019080805190602001909291905050509050600082602001511415610dac57600092505050610dd2565b60008260200151420390506000620151808281610dc557fe5b0490508083029450505050505b919050565b6000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b60405180604001604052806000815260200160008152509056fea2646970667358221220d29d5a4371b8f1009383b0c45e8489ae42548c24e50ae927fc394c2d79b025dd64736f6c634300060a0033"};

    public static final String BINARY = org.fisco.bcos.sdk.utils.StringUtils.joinAll("", BINARY_ARRAY);

    public static final String[] SM_BINARY_ARRAY = {};

    public static final String SM_BINARY = org.fisco.bcos.sdk.utils.StringUtils.joinAll("", SM_BINARY_ARRAY);

    public static final String[] ABI_ARRAY = {"[{\"inputs\":[{\"internalType\":\"address\",\"name\":\"_token\",\"type\":\"address\"},{\"internalType\":\"uint256\",\"name\":\"_minCoinDays\",\"type\":\"uint256\"}],\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"internalType\":\"address\",\"name\":\"user\",\"type\":\"address\"}],\"name\":\"CoinDaysBurned\",\"type\":\"event\"},{\"inputs\":[],\"name\":\"accessControl\",\"outputs\":[{\"internalType\":\"address\",\"name\":\"\",\"type\":\"address\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"address\",\"name\":\"user\",\"type\":\"address\"}],\"name\":\"burnCoinDays\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"address\",\"name\":\"user\",\"type\":\"address\"}],\"name\":\"calculateCoinDays\",\"outputs\":[{\"internalType\":\"uint256\",\"name\":\"\",\"type\":\"uint256\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"address\",\"name\":\"user\",\"type\":\"address\"}],\"name\":\"initUser\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[],\"name\":\"minCoinDays\",\"outputs\":[{\"internalType\":\"uint256\",\"name\":\"\",\"type\":\"uint256\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[],\"name\":\"reviewCore\",\"outputs\":[{\"internalType\":\"address\",\"name\":\"\",\"type\":\"address\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"address\",\"name\":\"_accessControl\",\"type\":\"address\"}],\"name\":\"setAccessControl\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"uint256\",\"name\":\"_minCoinDays\",\"type\":\"uint256\"}],\"name\":\"setMinCoinDays\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"address\",\"name\":\"_reviewCore\",\"type\":\"address\"}],\"name\":\"setReviewCore\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[],\"name\":\"token\",\"outputs\":[{\"internalType\":\"contract IERC20\",\"name\":\"\",\"type\":\"address\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"address\",\"name\":\"\",\"type\":\"address\"}],\"name\":\"userData\",\"outputs\":[{\"internalType\":\"uint256\",\"name\":\"lastBalance\",\"type\":\"uint256\"},{\"internalType\":\"uint256\",\"name\":\"lastUpdateTime\",\"type\":\"uint256\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"address\",\"name\":\"user\",\"type\":\"address\"}],\"name\":\"validateCoinDays\",\"outputs\":[{\"internalType\":\"bool\",\"name\":\"\",\"type\":\"bool\"}],\"stateMutability\":\"view\",\"type\":\"function\"}]"};

    public static final String ABI = org.fisco.bcos.sdk.utils.StringUtils.joinAll("", ABI_ARRAY);

    public static final String FUNC_ACCESSCONTROL = "accessControl";

    public static final String FUNC_BURNCOINDAYS = "burnCoinDays";

    public static final String FUNC_CALCULATECOINDAYS = "calculateCoinDays";

    public static final String FUNC_INITUSER = "initUser";

    public static final String FUNC_MINCOINDAYS = "minCoinDays";

    public static final String FUNC_REVIEWCORE = "reviewCore";

    public static final String FUNC_SETACCESSCONTROL = "setAccessControl";

    public static final String FUNC_SETMINCOINDAYS = "setMinCoinDays";

    public static final String FUNC_SETREVIEWCORE = "setReviewCore";

    public static final String FUNC_TOKEN = "token";

    public static final String FUNC_USERDATA = "userData";

    public static final String FUNC_VALIDATECOINDAYS = "validateCoinDays";

    public static final Event COINDAYSBURNED_EVENT = new Event("CoinDaysBurned", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}));
    ;

    protected CoinDayValidator(String contractAddress, Client client, CryptoKeyPair credential) {
        super(getBinary(client.getCryptoSuite()), contractAddress, client, credential);
    }

    public static String getBinary(CryptoSuite cryptoSuite) {
        return (cryptoSuite.getCryptoTypeConfig() == CryptoType.ECDSA_TYPE ? BINARY : SM_BINARY);
    }

    public List<CoinDaysBurnedEventResponse> getCoinDaysBurnedEvents(TransactionReceipt transactionReceipt) {
        List<EventValuesWithLog> valueList = extractEventParametersWithLog(COINDAYSBURNED_EVENT, transactionReceipt);
        ArrayList<CoinDaysBurnedEventResponse> responses = new ArrayList<CoinDaysBurnedEventResponse>(valueList.size());
        for (EventValuesWithLog eventValues : valueList) {
            CoinDaysBurnedEventResponse typedResponse = new CoinDaysBurnedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.user = (String) eventValues.getIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public void subscribeCoinDaysBurnedEvent(String fromBlock, String toBlock, List<String> otherTopics, EventCallback callback) {
        String topic0 = eventEncoder.encode(COINDAYSBURNED_EVENT);
        subscribeEvent(ABI,BINARY,topic0,fromBlock,toBlock,otherTopics,callback);
    }

    public void subscribeCoinDaysBurnedEvent(EventCallback callback) {
        String topic0 = eventEncoder.encode(COINDAYSBURNED_EVENT);
        subscribeEvent(ABI,BINARY,topic0,callback);
    }

    public String accessControl() throws ContractException {
        final Function function = new Function(FUNC_ACCESSCONTROL, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeCallWithSingleValueReturn(function, String.class);
    }

    public TransactionReceipt burnCoinDays(String user) {
        final Function function = new Function(
                FUNC_BURNCOINDAYS, 
                Arrays.<Type>asList(new Address(user)),
                Collections.<TypeReference<?>>emptyList());
        return executeTransaction(function);
    }

    public void burnCoinDays(String user, TransactionCallback callback) {
        final Function function = new Function(
                FUNC_BURNCOINDAYS, 
                Arrays.<Type>asList(new Address(user)),
                Collections.<TypeReference<?>>emptyList());
        asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForBurnCoinDays(String user) {
        final Function function = new Function(
                FUNC_BURNCOINDAYS, 
                Arrays.<Type>asList(new Address(user)),
                Collections.<TypeReference<?>>emptyList());
        return createSignedTransaction(function);
    }

    public Tuple1<String> getBurnCoinDaysInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function = new Function(FUNC_BURNCOINDAYS, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<String>(

                (String) results.get(0).getValue()
                );
    }

    public BigInteger calculateCoinDays(String user) throws ContractException {
        final Function function = new Function(FUNC_CALCULATECOINDAYS, 
                Arrays.<Type>asList(new Address(user)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeCallWithSingleValueReturn(function, BigInteger.class);
    }

    public TransactionReceipt initUser(String user) {
        final Function function = new Function(
                FUNC_INITUSER, 
                Arrays.<Type>asList(new Address(user)),
                Collections.<TypeReference<?>>emptyList());
        return executeTransaction(function);
    }

    public void initUser(String user, TransactionCallback callback) {
        final Function function = new Function(
                FUNC_INITUSER, 
                Arrays.<Type>asList(new Address(user)),
                Collections.<TypeReference<?>>emptyList());
        asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForInitUser(String user) {
        final Function function = new Function(
                FUNC_INITUSER, 
                Arrays.<Type>asList(new Address(user)),
                Collections.<TypeReference<?>>emptyList());
        return createSignedTransaction(function);
    }

    public Tuple1<String> getInitUserInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function = new Function(FUNC_INITUSER, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<String>(

                (String) results.get(0).getValue()
                );
    }

    public BigInteger minCoinDays() throws ContractException {
        final Function function = new Function(FUNC_MINCOINDAYS, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeCallWithSingleValueReturn(function, BigInteger.class);
    }

    public String reviewCore() throws ContractException {
        final Function function = new Function(FUNC_REVIEWCORE, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeCallWithSingleValueReturn(function, String.class);
    }

    public TransactionReceipt setAccessControl(String _accessControl) {
        final Function function = new Function(
                FUNC_SETACCESSCONTROL, 
                Arrays.<Type>asList(new Address(_accessControl)),
                Collections.<TypeReference<?>>emptyList());
        return executeTransaction(function);
    }

    public void setAccessControl(String _accessControl, TransactionCallback callback) {
        final Function function = new Function(
                FUNC_SETACCESSCONTROL, 
                Arrays.<Type>asList(new Address(_accessControl)),
                Collections.<TypeReference<?>>emptyList());
        asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForSetAccessControl(String _accessControl) {
        final Function function = new Function(
                FUNC_SETACCESSCONTROL, 
                Arrays.<Type>asList(new Address(_accessControl)),
                Collections.<TypeReference<?>>emptyList());
        return createSignedTransaction(function);
    }

    public Tuple1<String> getSetAccessControlInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function = new Function(FUNC_SETACCESSCONTROL, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<String>(

                (String) results.get(0).getValue()
                );
    }

    public TransactionReceipt setMinCoinDays(BigInteger _minCoinDays) {
        final Function function = new Function(
                FUNC_SETMINCOINDAYS, 
                Arrays.<Type>asList(new Uint256(_minCoinDays)),
                Collections.<TypeReference<?>>emptyList());
        return executeTransaction(function);
    }

    public void setMinCoinDays(BigInteger _minCoinDays, TransactionCallback callback) {
        final Function function = new Function(
                FUNC_SETMINCOINDAYS, 
                Arrays.<Type>asList(new Uint256(_minCoinDays)),
                Collections.<TypeReference<?>>emptyList());
        asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForSetMinCoinDays(BigInteger _minCoinDays) {
        final Function function = new Function(
                FUNC_SETMINCOINDAYS, 
                Arrays.<Type>asList(new Uint256(_minCoinDays)),
                Collections.<TypeReference<?>>emptyList());
        return createSignedTransaction(function);
    }

    public Tuple1<BigInteger> getSetMinCoinDaysInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function = new Function(FUNC_SETMINCOINDAYS, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<BigInteger>(

                (BigInteger) results.get(0).getValue()
                );
    }

    public TransactionReceipt setReviewCore(String _reviewCore) {
        final Function function = new Function(
                FUNC_SETREVIEWCORE, 
                Arrays.<Type>asList(new Address(_reviewCore)),
                Collections.<TypeReference<?>>emptyList());
        return executeTransaction(function);
    }

    public void setReviewCore(String _reviewCore, TransactionCallback callback) {
        final Function function = new Function(
                FUNC_SETREVIEWCORE, 
                Arrays.<Type>asList(new Address(_reviewCore)),
                Collections.<TypeReference<?>>emptyList());
        asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForSetReviewCore(String _reviewCore) {
        final Function function = new Function(
                FUNC_SETREVIEWCORE, 
                Arrays.<Type>asList(new Address(_reviewCore)),
                Collections.<TypeReference<?>>emptyList());
        return createSignedTransaction(function);
    }

    public Tuple1<String> getSetReviewCoreInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function = new Function(FUNC_SETREVIEWCORE, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<String>(

                (String) results.get(0).getValue()
                );
    }

    public String token() throws ContractException {
        final Function function = new Function(FUNC_TOKEN, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeCallWithSingleValueReturn(function, String.class);
    }

    public Tuple2<BigInteger, BigInteger> userData(String param0) throws ContractException {
        final Function function = new Function(FUNC_USERDATA, 
                Arrays.<Type>asList(new Address(param0)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
        List<Type> results = executeCallWithMultipleValueReturn(function);
        return new Tuple2<BigInteger, BigInteger>(
                (BigInteger) results.get(0).getValue(), 
                (BigInteger) results.get(1).getValue());
    }

    public Boolean validateCoinDays(String user) throws ContractException {
        final Function function = new Function(FUNC_VALIDATECOINDAYS, 
                Arrays.<Type>asList(new Address(user)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeCallWithSingleValueReturn(function, Boolean.class);
    }

    public static CoinDayValidator load(String contractAddress, Client client, CryptoKeyPair credential) {
        return new CoinDayValidator(contractAddress, client, credential);
    }

    public static CoinDayValidator deploy(Client client, CryptoKeyPair credential, String _token, BigInteger _minCoinDays) throws ContractException {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new Address(_token),
                new Uint256(_minCoinDays)));
        return deploy(CoinDayValidator.class, client, credential, getBinary(client.getCryptoSuite()), encodedConstructor);
    }

    public static class CoinDaysBurnedEventResponse {
        public TransactionReceipt.Logs log;

        public String user;
    }
}
