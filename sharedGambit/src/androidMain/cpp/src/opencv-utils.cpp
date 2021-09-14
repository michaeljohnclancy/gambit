//
// Created by mclancy on 11/09/2021.
//
//#include "opencv-utils.h"
//
//#include <algorithm>
//#include <opencv2/opencv.hpp>
//#include <opencv2/imgproc/imgproc.hpp>
//#include <jni.h>
//
//double medianMat(cv::Mat mat) {
//    mat = mat.reshape(0,1);// spread Input Mat to single row
//    std::vector<double> vecFromMat;
//    mat.copyTo(vecFromMat); // Copy Input Mat to vector vecFromMat
//    std::nth_element(vecFromMat.begin(), vecFromMat.begin() + vecFromMat.size() / 2, vecFromMat.end());
//    return vecFromMat[vecFromMat.size() / 2];
//}