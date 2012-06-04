package es.uji.security.crypto.config;

import javax.xml.bind.annotation.XmlRegistry;

@XmlRegistry
public class ObjectFactory
{
    public ObjectFactory()
    {
    }
    
    public ConfigManager createConfigManager()
    {
        return new ConfigManager();
    }
    
    public Keystore createKeystore()
    {
        return new Keystore();
    }
}
