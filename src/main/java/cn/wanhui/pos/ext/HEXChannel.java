package cn.wanhui.pos.ext;

import org.jpos.iso.*;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * @author yinheli
 */
public class HEXChannel extends BaseChannel {

    /**
     * constructor shared by server and client
     * ISOChannels (which have different signatures)
     */
    public HEXChannel() {
    }

    /**
     * constructs a client ISOChannel
     *
     * @param host server TCP Address
     * @param port server port number
     * @param p    an ISOPackager
     * @see org.jpos.iso.ISOPackager
     */
    public HEXChannel(String host, int port, ISOPackager p) {
        super(host, port, p);
    }

    /**
     * constructs a server ISOChannel
     *
     * @param p an ISOPackager
     * @throws java.io.IOException on error
     * @see org.jpos.iso.ISOPackager
     */
    public HEXChannel(ISOPackager p) throws IOException {
        super(p);
    }

    /**
     * constructs a server ISOChannel associated with a Server Socket
     *
     * @param p            an ISOPackager
     * @param serverSocket where to accept a connection
     * @throws java.io.IOException on error
     * @see org.jpos.iso.ISOPackager
     */
    public HEXChannel(ISOPackager p, ServerSocket serverSocket) throws IOException {
        super(p, serverSocket);
    }

    @Override
    protected void sendMessageHeader(ISOMsg m, int len) throws IOException {
        if (isOverrideHeader() && header != null) {
            m.setHeader(header);
            serverOut.write(header);
            return;
        }

        byte[] h = m.getHeader();
        if (h != null && h.length >= 5) {
            byte[] tmp = new byte[2];
            System.arraycopy(h, 1, tmp, 0, 2);
            System.arraycopy(h, 3, h, 1, 2);
            System.arraycopy(tmp, 0, h, 3, 2);
        } else {
            h = this.header;
        }

        if (h != null) {
            m.setHeader(h);
            this.serverOut.write(h);
        }
    }

    @Override
    protected void sendMessageLength(int len) throws IOException {
        byte[] b = new byte[2];
        b[0] = (byte) (len >> 8);
        b[1] = (byte) len;
        serverOut.write(b);
    }

    @Override
    protected int getMessageLength() throws IOException, ISOException {
        byte[] b = new byte[2];
        serverIn.readFully(b);
        return (b[0] & 0xFF) << 8 | b[1] & 0xFF;
    }

    @Override
    public void setHeader(String header) {
        super.setHeader(ISOUtil.str2bcd(header, false));
    }

    @Override
    protected void unpack(ISOMsg m, byte[] b) throws ISOException {
        super.unpack(m, b);
    }
}



