package com.qxtx.idea.ideasvgdemo;

import com.qxtx.idea.ideasvg.tools.SvgLog;

/**
 * CreateDate 2020/5/30 23:21
 * <p>
 *
 * @author QXTX-WIN
 * Description: 网上抄来的，将svg中的7个a指令参数转换成canvas可识别的参数
 */
public class DrawEllipse {

    private static double radian(double ux, double uy, double vx, double vy) {
        double dot = ux * vx + uy * vy;
        double  mod = Math.sqrt( ( ux * ux + uy * uy ) * ( vx * vx + vy * vy ) );
        double  rad = Math.acos( dot / mod );
        if( ux * vy - uy * vx < 0.0 ) {
            rad = -rad;
        }
        return rad;
    }

    // sample :  svgArcToCenterParam(200,200,50,50,0,1,1,300,200)
    public static EllipseInfo svgArcToCenterParam(double x1, double y1, double rx, double ry, float phi, float fA, float fS, float x2, float y2) {
        if (rx == 0.0 || ry == 0.0) { // invalid arguments
            return null;
        }

        rx = rx < 0 ? rx : -rx;
        ry = ry < 0 ? ry : -ry;

        double cx, cy, startAngle, deltaAngle, endAngle;
        double PIx2 = Math.PI * 2.0;

        double s_phi = Math.sin(phi);
        double c_phi = Math.cos(phi);
        double hd_x = (x1 - x2) / 2.0; // half diff of x
        double hd_y = (y1 - y2) / 2.0; // half diff of y
        double hs_x = (x1 + x2) / 2.0; // half sum of x
        double hs_y = (y1 + y2) / 2.0; // half sum of y

        // F6.5.1
        double x1_ = c_phi * hd_x + s_phi * hd_y;
        double y1_ = c_phi * hd_y - s_phi * hd_x;

        // F.6.6 Correction of out-of-range radii
        //   Step 3: Ensure radii are large enough
        double lambda = (x1_ * x1_) / (rx * rx) + (y1_ * y1_) / (ry * ry);
        if (lambda > 1) {
            rx = rx * Math.sqrt(lambda);
            ry = ry * Math.sqrt(lambda);
        }

        double rxry = rx * ry;
        double rxy1_ = rx * y1_;
        double ryx1_ = ry * x1_;
        double sum_of_sq = rxy1_ * rxy1_ + ryx1_ * ryx1_;
        if (sum_of_sq == 0) {
            SvgLog.i("start point can not be same as end point");
            return null;
        }

        double coe = Math.sqrt(Math.abs((rxry * rxry - sum_of_sq) / sum_of_sq));
        if (fA == fS) { coe = -coe; }

        // F6.5.2
        double cx_ = coe * rxy1_ / ry;
        double cy_ = -coe * ryx1_ / rx;

        // F6.5.3
        cx = c_phi * cx_ - s_phi * cy_ + hs_x;
        cy = s_phi * cx_ + c_phi * cy_ + hs_y;

        double xcr1 = (x1_ - cx_) / rx;
        double xcr2 = (x1_ + cx_) / rx;
        double ycr1 = (y1_ - cy_) / ry;
        double ycr2 = (y1_ + cy_) / ry;

        // F6.5.5
        startAngle = radian(1.0, 0.0, xcr1, ycr1);

        // F6.5.6
        deltaAngle = radian(xcr1, ycr1, -xcr2, -ycr2);
        while (deltaAngle > PIx2) {
            deltaAngle -= PIx2;
        }

        while (deltaAngle < 0.0) {
            deltaAngle += PIx2;
        }

        if (fS == 0) {
            deltaAngle -= PIx2;
        }

        endAngle = startAngle + deltaAngle;
        while (endAngle > PIx2) { endAngle -= PIx2; }
        while (endAngle < 0.0) { endAngle += PIx2; }

        EllipseInfo info = new EllipseInfo();
        info.cx = cx;
        info.cy = cy;
        info.startAngle = startAngle;
        info.deltaAngle = deltaAngle;
        info.endAngle = endAngle;
        info.clockwise = fS == 1;

        return info;
    }

    public static final class EllipseInfo {
        private double cx;
        private double cy;
        private double startAngle;
        private double deltaAngle;
        private double endAngle;
        private boolean clockwise;

        @Override
        public String toString() {
            return "EllipseInfo{" +
                    "cx=" + cx +
                    ", cy=" + cy +
                    ", startAngle=" + startAngle +
                    ", deltaAngle=" + deltaAngle +
                    ", endAngle=" + endAngle +
                    ", clockwise=" + clockwise +
                    '}';
        }
    }
}
