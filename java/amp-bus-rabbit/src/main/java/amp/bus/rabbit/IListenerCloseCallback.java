package amp.bus.rabbit;


import cmf.bus.IRegistration;


public interface IListenerCloseCallback {

    void onClose(IRegistration registration);
}
