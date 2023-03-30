package lsdi.fogworker.Services;

import com.espertech.esper.common.client.EPCompiled;
import com.espertech.esper.common.client.configuration.Configuration;
import com.espertech.esper.common.client.util.EventTypeBusModifier;
import com.espertech.esper.common.client.util.NameAccessModifier;
import com.espertech.esper.compiler.client.CompilerArguments;
import com.espertech.esper.compiler.client.EPCompileException;
import com.espertech.esper.compiler.client.EPCompiler;
import com.espertech.esper.compiler.client.EPCompilerProvider;
import com.espertech.esper.runtime.client.*;
import lombok.Data;
import lsdi.fogworker.DataTransferObjects.RuleRequestResponse;
import org.springframework.stereotype.Service;

import java.util.Map;

@Data
@Service
public class EsperService {
    private Configuration configuration;
    private CompilerArguments arguments;
    private EPCompiler compiler;
    private EPRuntime runtime;
    private static EsperService instance;

    public EsperService() {
        configuration = new Configuration();
        configuration.getCompiler().getByteCode().setAccessModifierEventType(NameAccessModifier.PUBLIC);
        configuration.getCompiler().getByteCode().setBusModifierEventType(EventTypeBusModifier.BUS);
        arguments = new CompilerArguments(configuration);
        compiler = EPCompilerProvider.getCompiler();
        runtime = EPRuntimeProvider.getDefaultRuntime(configuration);
    }

    public static EsperService getInstance() {
        if (instance == null) instance = new EsperService();
        return instance;
    }

    public EPCompiled compile(String epl) throws EPCompileException {
        return compiler.compile(epl, arguments);
    }

    public EPDeployment deploy(EPCompiled compiled) throws EPDeployException {
        return runtime.getDeploymentService().deploy(compiled);
    }

    public void undeploy(String deploymentId) throws EPUndeployException {
        runtime.getDeploymentService().undeploy(deploymentId);
    }

    public void sendEvent(Map<String, Object> event, String name) {
        runtime.getEventService().sendEventMap(event, name);
    }

    public EPStatement getStatement(String deploymentId, String statementName) {
        return runtime.getDeploymentService().getStatement(deploymentId, statementName);
    }

    public static String buildEPL(RuleRequestResponse rule) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("create map schema ");
        stringBuilder.append(rule.getEventType());
        stringBuilder.append(" as (");

        boolean isFirstEntry = true;
        for (Map.Entry<String, String> entry : rule.getEventAttributes().entrySet()) {
            if (!isFirstEntry) stringBuilder.append(", ");
            stringBuilder.append(entry.getKey());
            stringBuilder.append(" ");
            stringBuilder.append(entry.getValue());
            isFirstEntry = false;
        }

        stringBuilder.append(");\n");

        stringBuilder.append("@Name('");
        stringBuilder.append(rule.getName());
        stringBuilder.append("')\n");
        stringBuilder.append(rule.getDefinition());
        stringBuilder.append(";\n");
        return stringBuilder.toString();
    }
}
