package cn.wanhui.pos;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;

/**
 * @author yinheli
 */
public class Boot extends URLClassLoader implements FileFilter {

    private URLClassLoader sysClassloader = (URLClassLoader) ClassLoader.getSystemClassLoader();

    private Class<URLClassLoader> clazz= URLClassLoader.class;

    private Method sysClassloaderAddURLMethond;

    {
        try {
            sysClassloaderAddURLMethond = clazz.getDeclaredMethod("addURL", new Class[] { URL.class });
            sysClassloaderAddURLMethond.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * Constructs a new URLClassLoader for the given URLs. The URLs will be
     * searched in the order specified for classes and resources after first
     * searching in the specified parent class loader. Any URL that ends with
     * a '/' is assumed to refer to a directory. Otherwise, the URL is assumed
     * to refer to a JAR file which will be downloaded and opened as needed.
     * <p/>
     * <p>If there is a security manager, this method first
     * calls the security manager's <code>checkCreateClassLoader</code> method
     * to ensure creation of a class loader is allowed.
     *
     * @param urls   the URLs from which to load classes and resources
     * @param parent the parent class loader for delegation
     * @throws SecurityException if a security manager exists and its
     *                           <code>checkCreateClassLoader</code> method doesn't allow
     *                           creation of a class loader.
     * @see SecurityManager#checkCreateClassLoader
     */
    public Boot(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    /**
     * Constructs a new URLClassLoader for the specified URLs using the
     * default delegation parent <code>ClassLoader</code>. The URLs will
     * be searched in the order specified for classes and resources after
     * first searching in the parent class loader. Any URL that ends with
     * a '/' is assumed to refer to a directory. Otherwise, the URL is
     * assumed to refer to a JAR file which will be downloaded and opened
     * as needed.
     * <p/>
     * <p>If there is a security manager, this method first
     * calls the security manager's <code>checkCreateClassLoader</code> method
     * to ensure creation of a class loader is allowed.
     *
     * @param urls the URLs from which to load classes and resources
     * @throws SecurityException if a security manager exists and its
     *                           <code>checkCreateClassLoader</code> method doesn't allow
     *                           creation of a class loader.
     * @see SecurityManager#checkCreateClassLoader
     */
    public Boot(URL[] urls) {
        super(urls);
    }

    /**
     * Constructs a new URLClassLoader for the specified URLs, parent
     * class loader, and URLStreamHandlerFactory. The parent argument
     * will be used as the parent class loader for delegation. The
     * factory argument will be used as the stream handler factory to
     * obtain protocol handlers when creating new jar URLs.
     * <p/>
     * <p>If there is a security manager, this method first
     * calls the security manager's <code>checkCreateClassLoader</code> method
     * to ensure creation of a class loader is allowed.
     *
     * @param urls    the URLs from which to load classes and resources
     * @param parent  the parent class loader for delegation
     * @param factory the URLStreamHandlerFactory to use when creating URLs
     * @throws SecurityException if a security manager exists and its
     *                           <code>checkCreateClassLoader</code> method doesn't allow
     *                           creation of a class loader.
     * @see SecurityManager#checkCreateClassLoader
     */
    public Boot(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
        super(urls, parent, factory);
    }

    /**
     * Tests whether or not the specified abstract pathname should be
     * included in a pathname list.
     *
     * @param pathname The abstract pathname to be tested
     * @return <code>true</code> if and only if <code>pathname</code>
     *         should be included
     */
    @Override
    public boolean accept(File pathname) {
        return pathname.isDirectory() || pathname.getName().toLowerCase().endsWith(".jar");
    }

    /**
     * Loads the class with the specified <a href="#name">binary name</a>.
     * This method searches for classes in the same manner as the {@link
     * #loadClass(String, boolean)} method.  It is invoked by the Java virtual
     * machine to resolve class references.  Invoking this method is equivalent
     * to invoking {@link #loadClass(String, boolean) <tt>loadClass(name,
     * false)</tt>}.  </p>
     *
     * @param name The <a href="#name">binary name</a> of the class
     * @return The resulting <tt>Class</tt> object
     * @throws ClassNotFoundException If the class was not found
     */
    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return super.loadClass(name);
    }

    private void add2sys(URL url) {
        try {
            sysClassloaderAddURLMethond.invoke(sysClassloader, new Object[]{url});
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * Appends the specified URL to the list of URLs to search for
     * classes and resources.
     *
     * @param url the URL to be added to the search path of URLs
     */
    @Override
    protected void addURL(URL url) {
        super.addURL(url);
        add2sys(url);
    }

    private void add(File... files) throws MalformedURLException {
        for (File file : files) {
            if (file == null) continue;
            File[] fs = file.listFiles(this);
            if (fs == null) continue;
            for (File f : fs) {
                if (f.isFile()) {
                    URL url = f.toURI().toURL();
                    addURL(url);
                } else {
                    add(f);
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        ClassLoader loader = Boot.class.getClassLoader();
        Boot boot = new Boot(new URL[0], Boot.class.getClassLoader());
        URL root = loader.getResource("");
        boot.addURL(root);
        Thread.currentThread().setContextClassLoader(boot);
        File rootFile = new File(root.getFile()).getParentFile();
        String[] dirs = {"libs", "lib", "third-lib", "app"};
        for (String dir : dirs) {
            File f = new File(rootFile, dir);
            if (f.exists() && f.isDirectory()) {
                boot.add(f);
            }
        }

        Class<?> c = boot.loadClass("org.jpos.q2.Q2", true);
        c.getMethod("main", args.getClass()).invoke(null, new Object[]{args});
    }
}


